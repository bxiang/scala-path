package com.abc.cats.tagless

import cats.implicits._
import cats.Monad

class Service[F[_] : Monad](userRepo: UserRepo[F], ticketRepo: TicketRepo[F]) {

  def updateTicketStatus(ticketId: String, st: String): F[Either[String, Unit]] = {
    ticketRepo.findTicket(ticketId).flatMap {
      case None => implicitly[Monad[F]].pure(Left(s"Ticket $ticketId not found"))
      case Some(ticket) =>
        val updatedTicket = ticket.copy(status = st)
        ticketRepo.updateTicket(updatedTicket).map(Right(_))
    }
  }

  def assignTicket(ticketId: String, userId: String): F[Either[String, Unit]] = {
    ticketRepo.findTicket(ticketId).flatMap {
      case None => implicitly[Monad[F]].pure(Left(s"Ticket $ticketId not found"))
      case Some(ticket) =>
        userRepo.findUser(userId).flatMap {
          case None => implicitly[Monad[F]].pure(Left(s"User $userId not found"))
          case Some(user) =>
            val updatedTicket = ticket.copy(assignee = Some(user))
            ticketRepo.updateTicket(updatedTicket).map(Right(_))
        }
    }
  }

}
