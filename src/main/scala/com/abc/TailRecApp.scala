package com.abc

import scala.annotation.tailrec

object TailRecApp extends App {

  def sum(list: List[Int]): Int = {

    @tailrec
    def loop(list: List[Int], currentSum: Int): Int = {
      list match {
        case Nil => currentSum
        case x :: xs => loop(xs, currentSum + x)
      }
    }

    loop(list, 0)
  }

  def factorial(i: Int): Int = {

    @tailrec
    def loop(x: Int, result: Int): Int = {
      if (x <= 0 ) result
      else loop(x -1, x * result)
    }

    loop(i, 1)
  }

  val s = sum(List(1, 2, 3, 4, 5))
  println(s)

  val f = factorial(4)
  println(f)

}
