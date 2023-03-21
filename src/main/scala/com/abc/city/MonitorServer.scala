package monitoring

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Ref
import com.typesafe.config.ConfigFactory
import fs2.Stream
import UncachedIndexImplicit._
import io.circe.generic.auto._
import io.circe.syntax._
import monitoring.rod.RodChange
import org.http4s._
import org.http4s.asynchttpclient.client.AsyncHttpClient
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.staticcontent._
import snap.{SnapCheck, SnapEnvironment, SnapUrl}
import _root_.snap._
import cats.effect.unsafe.implicits.global
import monitoring.authentication.{AuthenticationConfig, JwtCookieSecureTokenOperations, Oauth2RedirectUri, TpsConfig, TpsSingleSignOnAuthenticator, UnentitledUser}
import monitoring.extuser.ExtUserService
import monitoring.jira.SimpleJiraIssue
import monitoring.report.SnapUsageReport
import monitoring.report.util.CrackleConf
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.impl.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.headers.{Location, `Content-Disposition`, `Content-Type`}
import org.typelevel.ci.CIString
import tsec.mac.jca.{HMACSHA256, MacSigningKey}
import tsec.common._

import java.io.File
import java.time.{Instant, LocalDate}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

/** Purpose: http4s service that contains set-up and various routes */
object MonitoringServer extends IOApp {
  val tpsPwd: String       = "43xSEFWqPTHocsNy8kcWpA=="
  val tpsConfig: TpsConfig = TpsConfig("https://tpsoauth.nam.nsroot.net", tpsPwd)

  val signingKey: MacSigningKey[HMACSHA256] = decryptKey(
    "aCJT/zcIDH+0J3iVFyuVhTHmM02IhFzzWznNRd5kBrcVdXQkRSBZWSIv6B4plULLCfLY5tzqHFbTLGLWfU8kBdMk4U4neksnJbIJPJwBB9U="
  )

  val authenticationConfig: AuthenticationConfig = AuthenticationConfig(tpsConfig, signingKey)
  //AuthorisationStrategy.fromEnvironment(sys.env))

  private object OriginalUri extends OptionalQueryParamDecoderMatcher[Uri]("original")
  private object AuthCode    extends QueryParamDecoderMatcher[String]("code")
  val httpClient: Client[IO] = JavaNetClientBuilder[IO].create
  val ssoAuthenticator =
    new TpsSingleSignOnAuthenticator(httpClient, authenticationConfig.tps)

  private val ioClock                   = IO(Instant.now())
  private val authenticationTokenExpiry = 12.hours

  private val jwtCookieOperations =
    new JwtCookieSecureTokenOperations[IO](authenticationConfig, ioClock, authenticationTokenExpiry)

  private val reportRunningRef: Ref[IO, Boolean] = Ref.unsafe[IO, Boolean](false)
  override def run(args: List[String]): IO[ExitCode] =
    IO(System.setProperty("jdk.tls.maxCertificateChainLength", "15")) *> runApp.compile.drain
      .as(ExitCode.Success)

  private def runApp: Stream[IO, ExitCode] = {
    for {
      _ <- Stream.eval(setEnvironment())
      config <- Stream.eval[IO, CombinedConfig](IO {
        import pureconfig._
        import pureconfig.generic.auto._
        import SnapConfigReaders._
        ConfigSource.fromConfig(ConfigFactory.load()).loadOrThrow[CombinedConfig]
      })
      couchbaseCluster <- Stream.resource(
        CouchbaseClient.clusterResource(config.couchbase)
      )
      repo = new RodReleaseRepository.CouchbaseBasedRodReleaseRepository(couchbaseCluster)
      httpClient <- Stream.resource(
        AsyncHttpClient.resource[IO](SnapTrustManagerFactory.configureAsync)
      )
      exitCode <- BlazeServerBuilder[IO](ExecutionContext.global)
        .bindHttp(config.port, "0.0.0.0")
        .withResponseHeaderTimeout(4.minutes)
        .withIdleTimeout(5.minutes)
        .withHttpApp(
          Router(
            "/" -> routes(
              repo = repo,
              logs = Logs.real(config.aim, httpClient),
              combinedConfig = config,
              announcer = Announcer.from(
                repo
                  .loadChanges[RodChange](RodReleaseRepository.SnapComponentName)
                  .map(
                    lr =>
                      lr.sortBy(_.releaseEndDateMillis)
                        .reverse
                        .take(4)
                        .map(rc => Announcer.SimpleRodRelease.fromRodChange(rc))
                  ),
                config.jira
              )
            )(httpClient),
            "/" -> RequestTestEmailRoute.route(combinedConfig = config),
            "/" -> fileService(FileService.Config[IO]("assets")).uncachedIndex,
            "/" -> resourceServiceBuilder[IO]("/assets").toRoutes.uncachedIndex,
            "/" -> HttpRoutes
              .of[IO] {
                case req @ GET -> Root =>
                  resourceServiceBuilder[IO]("/assets").toRoutes
                    .run(req.withUri(uri"/index2.html"))
                    .getOrElseF(NotFound(""))
              }
              .uncachedIndex
          ).orNotFound
        )
        .withoutBanner
        .serve
    } yield exitCode
  }

  private def routes(
      repo: RodReleaseRepository,
      logs: Logs,
      combinedConfig: CombinedConfig,
      announcer: Announcer
  )(implicit httpClient: Client[IO]): HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case GET -> Root / "changes" =>
        //import io.circe.generic.auto._
        repo
          .loadChanges[RodChange](RodReleaseRepository.SnapComponentName)
          .flatMap(changes => Ok(changes.asJson))

      case req @ GET -> Root / "consolidated-environments" =>
        def getUrlInfo(snapUrl: SnapUrl): IO[SnapCheck] =
          HttpClientBasedSnapChecker(httpClient, combinedConfig.snap).checkEndpoint(snapUrl)
        import cats.implicits._
        SnapEnvironment.All.parTraverse(_.traverse(url => getUrlInfo(url))).flatMap {
          environments =>
            //import io.circe.generic.auto._
            Ok(environments)
        }
      case GET -> Root / "teamcity" =>
        //import io.circe.generic.auto._
        combinedConfig.teamcity
          .getLatestBuilds(TeamCity.SnapBuildTypes: _*)
          .flatMap(results => Ok(results))
      case req @ GET -> Root / "logs" =>
        jwtCookieOperations
          .extractToken(req)
          .filter { securedRequest =>
            val userId = securedRequest.identity.name.value
            AuthorizedMonitoringUsers.usersWhoCanSeeLogs.contains(userId)
          }
          .semiflatMap { _ =>
            Logs.serve(logs, req)
          }
          .getOrElseF(Forbidden("Not authorized to see the logs"))
      case GET -> Root / "jira" =>
        //import io.circe.generic.auto._
        import JIRA._
        import JIRA.JiraInstantDecoder._

        combinedConfig.jira
          .fetchJiras[SimpleJiraIssue](MostRecentJiraQuery)
          .flatMap(container => Ok(container.issues))
      case request @ GET -> Root / "announce" =>
        import org.http4s.scalatags._
        announcer.prepInfo.flatMap { prep =>
          Ok(
            prep.renderHtml
          )
        }
      case request @ GET -> Root / "login" =>
        SeeOther(
          Location(
            (tpsConfig.uri / "oauth" / "authorize")
              .withQueryParam("response_type", "code")
              .withQueryParam("scope", "snap-web")
              .withQueryParam("client_id", "snap")
              .withQueryParam(
                "redirect_uri",
                Oauth2RedirectUri.build(request, Some("/")).renderString
              )
          )
        )
      case request @ GET -> Root / "report" =>
        jwtCookieOperations
          .extractToken(request)
          .filter { securedRequest =>
            val userId = securedRequest.identity.name.value
            AuthorizedMonitoringUsers.authorisedUsers.contains(userId)
          }
          .semiflatMap { _ =>
            val reportFile = SnapUsageReport.reportFile
            for {
              report <- SnapUsageReport.generateReport(reportFile, reportRunningRef).uncancelable
              resp <- report match {
                case Right(r) => Ok(r).map(
                  _.withHeaders(
                    `Content-Disposition`("attachment", Map(CIString("filename") -> reportFile)),
                    `Content-Type`.parse("text/plain")))
                case Left(message) => Ok(message)
              }
            } yield resp
          }
          .getOrElseF(Ok("Not authenticated"))
      case request @ GET -> Root / "authenticate" :? OriginalUri(uri) :? AuthCode(code) =>
        val targetLocation = uri.map(_.renderString)
        for {
          user <- ssoAuthenticator
            .authenticate(code, Oauth2RedirectUri.build(request, targetLocation))
            .attempt
          response <- user.fold(
            redirectToOriginalLocationSoUserCanTryAgain(targetLocation),
            authorise(request, targetLocation, _)
          )
        } yield response
      case request @ GET -> Root / "external-users" =>
        jwtCookieOperations
          .extractToken(request)
          .filter { securedRequest =>
            val userId = securedRequest.identity.name.value
            AuthorizedMonitoringUsers.usersWhoCanSeeExternalUsers.contains(userId)
          }
          .semiflatMap { _ =>
            val snapEnv = SnapEnvironment.AllPlusLocal
              .find(env => request.params.get("env").contains(env.name))
              .getOrElse(SnapEnvironment.PROD)
            ExtUserService.getExternalUsers(snapEnv)
          }
          .semiflatMap { extUsers =>
            import org.http4s.scalatags._
            Ok(ExtUserService.renderHtml(extUsers))
          }
          .getOrElseF(NotFound())
      case request @ GET -> Root / "check-auth" =>
        jwtCookieOperations
          .extractToken(request)
          .filter { securedRequest =>
            val userId = securedRequest.identity.name.value
            AuthorizedMonitoringUsers.usersWhoCanSeeExternalUsers.contains(userId)
          }
          .semiflatMap { res =>
            Ok(s"authenticated: ${res.identity}")
          }
          .getOrElseF(Ok("Not authenticated"))
    }
  }

  private def setEnvironment() = {
    val trustStore = new File(CrackleConf.TestCaCertsPath)
    if (trustStore.exists())
      IO {
        sys.props += ("javax.net.ssl.trustStore" -> trustStore.getAbsolutePath)
      } else
      IO.raiseError(new Exception(s"Trust store ${trustStore.getAbsolutePath} not found."))
  }

  def decryptKey(value: String): MacSigningKey[HMACSHA256] = {
    val decrypted = _root_.snap.util.Crypto.decrypt(value)
    HMACSHA256.buildKey[IO](decrypted.hexBytesUnsafe).unsafeRunSync()
  }

  private def redirectToOriginalLocationSoUserCanTryAgain(
      uri: Option[String]
  )(error: Throwable): IO[Response[IO]] = {
    SeeOther(Location(Uri.unsafeFromString(uri.getOrElse("/"))))
  }

  private def authorise(
      request: Request[IO],
      uri: Option[String],
      user: UnentitledUser
  ): IO[Response[IO]] = {
    if (AuthorizedMonitoringUsers.authorisedUsers.contains(user.name.value)) {
      jwtCookieOperations.embedToken(user)(
        uri.fold(Ok())(
          u =>
            SeeOther(Location(Uri.unsafeFromString(u)))
              .map(_.addCookie("loggedin", user.name.value))
        )
      )
    } else {
      uri.fold(Ok())(u => SeeOther(Location(Uri.unsafeFromString(u))))
    }
  }

}
