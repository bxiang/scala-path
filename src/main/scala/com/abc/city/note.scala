package com.abc.city

private def submitPriceRequest(body: Json): F[QuestSubmitResponse] =
    for {
      _ <- Sync[F].delay{println(s"to Quest: ${body.deepDropNullValues.noSpaces}")}
      resp <- httpClient
              .expect[QuestSubmitResponse] (
                                       POST(body, config.apiGatewayUri / "api" / "pricing" / "v1" / "price" withQueryParam ("platform", "SNAP")),
                                       "Unable to price")
      _ <- Sync[F].delay{ println(s"from Quest: $resp")}
    } yield resp


def fromFile[F[_]: Async](sourceFile: String): Stream[F, Byte] = {
    Files[F].readAll(Path(sourceFile))
//      .through(text.utf8.decode)
//      .through(text.lines)
//      .intersperse("\n")
//      .through(text.utf8.encode)
  }

//    val url = (config.apiGatewayUri / "Contact" / "GetCVContactInfo").withQueryParam("cvid", cvId)
//    for {
//      _ <- IO {println(s"getExternalClientDetails: ${url.toString}")}
//      x <-  httpClient
//        .expect[GetCVContactInfoResponse](
//          IO.delay { GET((config.apiGatewayUri / "Contact" / "GetCVContactInfo").withQueryParam("cvid", cvId))
//            .withHeaders(
//              Header.Raw(CIString("X-ciextapi-fid"), config.apiFunctionalId),
//              Header.Raw(CIString("Content-Type"), "application/json-patch+json"),
//              Header.Raw(CIString("Accept"), "text/plain"))
//          },
//          s"Unable to fetch external client details for $cvId"
//        )
//        .map(_.data.headOption)
//      _ <- IO{ println(s"External user $cvId -> $x")}
//    } yield x


      mongo.findQuotes()
        .map(quote2QuoteReport)
//        .evalTap(r => log.info(r.toString))
        .compile
        .toList
//        .flatTap(r => log.info(s"Total size ${r.size}"))
        .map(_.zipWithIndex.groupBy(_._1).toList.map{case (q, c) => q.copy(snap = c.size)}.sortBy(r=> (r.user, r.week)))

 val client: Client[IO] = JavaNetClientBuilder[IO](Blocker.liftExecutionContext(executionContext)).withProxy(NO_PROXY).create
    Logger(logBody = true, logHeaders = true)(client)
      .expect[List[ExtUser]](POST(cvRequestJson, cvUri))
      .map(_.filter(_.isClient))

      _ <- Sync[F].delay(println(s"pricing result"))


  import io.circe.syntax._
  def createQuote(dealId: DealId, request: Request[IO], user: User, requestedFairValue: Option[Boolean]): IO[Response[IO]] = {
    val response = for {
      priceRequest <- TracingIO.lift(request.as[UnpricedRequest])
      _ <- ReaderT.liftF(request.bodyText.compile.string.map(b => println(s"from GUI: $b")))
      response     <- permissionedPricing.createQuote(dealId, PricingRequest(priceRequest, PricingOptions(requestedFairValue.getOrElse(false))), user).value
      _ <- ReaderT.liftF(IO{ println(s"to GUI: ${response.toOption.get.asJson.noSpaces}") })
    } yield response


crackle / Test / runMain crackle.MakeApiExamples Write