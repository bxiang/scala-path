package com.abc.cats.tagless

import cats.Id

import scala.concurrent.Future


trait IdUserInterpreter extends UserRepo[Id] {

  override def findUser(userId: String): Id[Option[User]] = {
    println(s"Id User found $userId")
    Some(User(userId, "Brian", "Xiang"))
  }
}

trait FutureUserInterpreter extends UserRepo[Future] {

  override def findUser(userId: String): Future[Option[User]] = {
    userId match {
      case "1" =>
        println(s"Future User could not find $userId")
        Future.successful(None)
      case _ =>
        println(s"Future User found $userId")
        Future.successful(Some(User(userId, "Brian", "Xiang")))
    }
  }
}

trait IdTicketInterpreter extends TicketRepo[Id] {

  override def findTicket(ticketId: String): Id[Option[Ticket]] = {
    println(s"Id Ticket found $ticketId")
    Some(Ticket(ticketId, "blah description", "status"))
  }

  override def updateTicket(ticket: Ticket): Id[Unit] = {
    println(s"Id Ticket updated $ticket")
  }
}


trait FutureTicketInterpreter extends TicketRepo[Future] {

  override def findTicket(ticketId: String): Future[Option[Ticket]] = {
    ticketId match {
      case "123" =>
        println(s"Future Ticket could not find $ticketId")
        Future.successful(None)
      case _ =>
        println(s"Future Ticket found $ticketId")
        Future.successful(Some(Ticket(ticketId, "blah description", "status")))
    }
  }

  override def updateTicket(ticket: Ticket): Future[Unit] = {
    println(s"Future Ticket updated $ticket")
    Future.successful()
  }
}