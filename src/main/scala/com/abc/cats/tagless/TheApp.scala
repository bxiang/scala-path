package com.abc.cats.tagless

import cats.implicits._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object TheApp extends App {
  val service = new Service(new FutureUserInterpreter {}, new FutureTicketInterpreter {})
  val result = service.updateTicketStatus("123", "Completed")
  val output: Either[String, Unit] = Await.result(result, 2 seconds)
  println(s"Output: $output")

  val service2 = new Service(new FutureUserInterpreter {}, new FutureTicketInterpreter {})
  val result2 = service2.updateTicketStatus("456", "Completed")
  val output2: Either[String, Unit] = Await.result(result2, 2 seconds)
  println(s"Output: $output2")

  val service3 = new Service(new FutureUserInterpreter {}, new FutureTicketInterpreter {})
  val result3 = service3.assignTicket("456", "666")
  val output3: Either[String, Unit] = Await.result(result3, 2 seconds)
  println(s"Output: $output3")

  val service4 = new Service(new FutureUserInterpreter {}, new FutureTicketInterpreter {})
  val result4 = service4.assignTicket("123", "666")
  val output4: Either[String, Unit] = Await.result(result4, Duration(2, "second"))
  println(s"Output: $output4")

  val service5 = new Service(new FutureUserInterpreter {}, new FutureTicketInterpreter {})
  val result5 = service5.assignTicket("456", "1")
  val output5: Either[String, Unit] = Await.result(result5, Duration(2, "second"))
  println(s"Output: $output5")

  val service6= new Service(new IdUserInterpreter {}, new IdTicketInterpreter {})
  val result6 = service6.assignTicket("456", "888")
  println(s"Output: $result6")

}
