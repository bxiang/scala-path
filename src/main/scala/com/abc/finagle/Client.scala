package com.abc.finagle

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}

object Client extends App {

  val client: Service[Request, Response] = Http.newService("www.scala-lang.org:80")

  val request = Request(Method.Get, "/")
  request.host = "www.scalay-lang.org"

  val response: Future[Response] = client(request)

  Await.result(response.onSuccess { rep: Response =>
    println("GET success: " + rep)
  })

}
