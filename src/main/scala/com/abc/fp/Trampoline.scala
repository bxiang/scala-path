package com.abc.fp

import scala.annotation.tailrec

object Trampoline extends App {

  sealed trait Bounce[A]
  case class Done[A](result: A) extends Bounce[A]
  case class Call[A](thunk: () => Bounce[A]) extends Bounce[A]

  def trampoline[A](bounce: Bounce[A]): A = bounce match {
    case Call(thunk) => trampoline(thunk())
    case Done(x) => x
  }

  def factorial(n: Int, product: BigInt): Bounce[BigInt] = {
    if (n < 2) Done(product)
    else Call(() => factorial(n - 1, n * product))
  }

  def factorial(n: Int): BigInt = {
    @tailrec
    def factorialRec(n: Int, result: BigInt): BigInt = {
      if (n < 2) result
      else factorialRec(n -1, n * result)
    }

    factorialRec(n, 1)
  }

//  println(trampoline(factorial(1, 1)))
  println(factorial(100000))



}
