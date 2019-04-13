//
//import cats.Applicative
//import cats.data.ValidatedNel
//import cats.syntax.validated._
//import cats.instances.list._
//
//type Error = String
//type ErrorOr[A] = ValidatedNel[Error, A]
//
//val errorOrFirstName: ErrorOr[String] = "empty first name".validNel
//val errorOrMiddleName: ErrorOr[String] = "middle name".validNel
//val errorOrLastName: ErrorOr[String] = "empty last name".invalidNel
//val errorsOrFullName0: ErrorOr[String] = Applicative[ErrorOr].map2(errorOrFirstName, errorOrMiddleName)(_ + " " + _)
//val errorsOrFullName1: ErrorOr[String] = Applicative[ErrorOr].map2(errorsOrFullName0, errorOrLastName)(_ + " " + _)
//
//println(errorsOrFullName0)
//println(errorsOrFullName1)

//import cats.data._

import com.abc.util.StringUtil._

val x12 = "1352".toIntOpt

