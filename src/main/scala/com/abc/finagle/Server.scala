package com.abc.finagle

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}

object Server extends App {

  val service = new Service[Request, Response] {
    def apply(req: Request): Future[Response] = {
      println("Got request")
      Future.value(
        Response(req.version, Status.Ok)
      )
    }
  }

  val server = Http.serve(":8080", service)
  Await.ready(server)
}
