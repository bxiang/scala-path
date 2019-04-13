package com.abc

import scala.annotation.tailrec

object TailRecApp extends App {

  def sum(list: List[Int]): Int = {

    @tailrec
    def sumWithAccumulator(list: List[Int], currentSum: Int): Int = {
      list match {
        case Nil => currentSum
        case x :: xs => sumWithAccumulator(xs, currentSum + x)
      }
    }

    sumWithAccumulator(list, 0)
  }

  val s = sum(List(1, 2, 3, 4, 5))
  println(s)

}
