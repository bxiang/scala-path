package com.abc.cats

import cats.Applicative
import cats.data.ValidatedNel
import cats.syntax.validated._
import cats.instances.list._

object ValidatedNelApp extends App {

  type Error = String
  type ErrorOr[A] = ValidatedNel[Error, A]

  val errorOrTitle: ErrorOr[String] = "title".validNel
  val errorOrFirstName: ErrorOr[String] = "first name".validNel
  val errorOrMiddleName: ErrorOr[String] = "empty middle name".invalidNel
  val errorOrLastName: ErrorOr[String] = "empty last name".invalidNel
  val errorsOrFullName0: ErrorOr[String] = Applicative[ErrorOr].map2(errorOrTitle, errorOrFirstName)(_ + " " + _)
  val errorsOrFullName1: ErrorOr[String] = Applicative[ErrorOr].map2(errorsOrFullName0, errorOrMiddleName)(_ + " " + _)
  val errorsOrFullName2: ErrorOr[String] = Applicative[ErrorOr].map2(errorsOrFullName1, errorOrLastName)(_ + " " + _)

  println(errorsOrFullName0)
  println(errorsOrFullName1)
  println(errorsOrFullName2)
}
