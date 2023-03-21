package monitoring.snap

import cats.effect._
import cats.implicits._
import monitoring.snap.Implicits._
import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html._
import Implicits._
import cats.data.NonEmptyList
import cats.effect.unsafe.implicits.global
import monitoring.jira.SimpleJiraIssue
import monitoring.rod.RodChange
import monitoring.snap.SnapCheck.{AStatus, CombinedResponse}
import monitoring.teamcity.TCBuild

import java.time.{Instant, ZoneId}
import scala.concurrent.duration.DurationInt

/**
  * Purpose: Render the main view of the Frontend. This is a React component though doesn't feel like it :-)
  */
@react class MonitoringFrontendComponent extends Component {
  type Props = Unit

  case class State(
      lastUpdate: Option[Instant],
      currently: List[SnapEnvironment[TwoStates]],
      cancel: IO[Unit],
      releases: List[RodChange],
      verifications: List[TCBuild],
      jiras: List[SimpleJiraIssue]
  )

  override def initialState: State =
    State(
      lastUpdate = None,
      currently = SnapEnvironment.All.map(_.map(v => Left(v))),
      cancel = IO.unit,
      releases = Nil,
      verifications = Nil,
      jiras = Nil
    )

  override def componentDidMount(): Unit = {
    def doUpdate: IO[Unit] =
      FetchCheck.doCheck
        .map(_.map(_.map(v => Right(v))))
        .flatTap(res => IO(setState(_.copy(lastUpdate = Some(Instant.now()), currently = res))))
        .void

    def fetchOther: IO[Unit] = {
      List(
        FetchRod.doCheck.flatMap(rod => IO(setState(_.copy(releases = rod)))),
        FetchTeamCity.doCheck.flatMap(tc => IO(setState(_.copy(verifications = tc)))),
        FetchJIRA.doCheck.flatMap(jiras => IO(setState(_.copy(jiras = jiras))))
      ).sequence_
    }

    (fs2.Stream.eval(doUpdate *> fetchOther) ++ fs2.Stream
      .fixedRate[IO](20.seconds)
      .evalMap(_ => doUpdate)).compile.drain.background.allocated
      .flatMap(canceller => IO(setState(_.copy(cancel = canceller._1.void))))
      .unsafeRunAndForget()
  }

  override def componentWillUnmount(): Unit = {
    state.cancel.flatMap(_ => IO(setState(_.copy(cancel = IO.unit)))).unsafeRunAndForget()
  }

  def render(): ReactElement = {
    div(className := "App")(
      header(className := "App-header")(
        h1(className := "App-title")("SNAP Monitoring v2"),
        a(href := "/login", target := "_self", "Login"),
        NavLinks.navLinks
      ),
      div(className := "stimulator"),
      state.lastUpdate match {
        case None         => p("Waiting for an update...")
        case Some(update) => p("Last update: ", update.toString)
      },
      p(
        className := "test-health",
        "Test health: ",
        NonEmptyList.fromList(state.verifications) match {
          case None => "Loading..."
          case Some(vers) =>
            RenderVerifications(vers)
        }
      ),
      div(
        className := "envs",
        RenderEnvironments(state.currently)
      ),
      section(
        className := "quest",
        h2("Quest environments"),
        table(
          thead(
            tr(th("Environment"), th("Swagger"), th("UI"))
          ),
          tbody(QuestEnvironment.All.map {
            case (name, QuestEnvironment(swaggerUrl, uiUrl)) =>
              tr(th(name), td(a(href := swaggerUrl, swaggerUrl)), td(a(href := uiUrl, uiUrl)))
          })
        )
      ),
      h2("Releases"),
      NonEmptyList.fromList(state.releases) match {
        case None => p("Loading...")
        case Some(releases) =>
          RenderReleases.apply(releases)
      },
      h2("Recent JIRAs"),
      NonEmptyList.fromList(state.jiras) match {
        case None => p("Loading...")
        case Some(jiras) =>
          RenderJIRAs.apply(jiras)
      }
    )
  }

}
