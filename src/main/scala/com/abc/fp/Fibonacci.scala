package com.abc.fp

import scala.annotation.tailrec

object Fibonacci extends App {

  // 0, 1, 1, 2, 3, 5, 8, 13
  def fib(n: Int): BigInt = {
    @tailrec
    def fibRec(i: Int, a: Int, b: Int): BigInt = i match {
      case 0 => a
      case 1 => b
      case _ => fibRec(i - 1, b, a + b)
    }

    fibRec(n, 0, 1)
  }

  println(fib(0))
  println(fib(1))
  println(fib(2))
  println(fib(3))
  println(fib(4))
  println(fib(5))
}
