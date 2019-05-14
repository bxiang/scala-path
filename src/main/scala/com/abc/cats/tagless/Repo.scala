package com.abc.cats.tagless

case class User(userId: String, firstName: String, lastName: String)

case class Ticket(ticketId: String, desc: String, status: String, assignee: Option[User] = None)

trait UserRepo[F[_]] {

  def findUser(userId: String): F[Option[User]]

}

trait TicketRepo[F[_]] {

  def findTicket(ticketId: String): F[Option[Ticket]]

  def updateTicket(ticket: Ticket): F[Unit]

}


