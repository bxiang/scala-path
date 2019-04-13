package com.abc.tagless_final

import cats.Monad
import cats.implicits._
import com.abc.tagless_final.authn.Dsl.UserRepositoryState
import com.abc.tagless_final.authn.domain.AuthnError
import com.abc.tagless_final.authn.{Dsl => AuthnDsl}
import com.abc.tagless_final.shared.domain.{EmailAddress, User}
import shapeless.tag

import scala.language.higherKinds

object Main extends App {

  def registerAndLogin[F[_] : Monad](implicit authnDsl: AuthnDsl[F]): F[Either[AuthnError, User]] = {

    val email = tag[EmailAddress]("john@doe.com")
    val password = "swordfish"

    for {
      _ <- authnDsl.register(email, password)
      authenticated <- authnDsl.authn(email, password)
    } yield authenticated
  }

  val userRepositoryState = registerAndLogin[UserRepositoryState]

  val result = userRepositoryState.runEmpty

  val (users, authenticated) = result.value

  println("Authenticated: " + authenticated)
  println("Registered users: " + users)
}
