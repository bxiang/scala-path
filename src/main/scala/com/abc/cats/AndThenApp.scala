package com.abc.cats


object AndThenApp extends App {

  val twice: Int => Int = x => x * 2

  val countCats: Int => String = x => if (x == 1) "1 cat" else s"$x cats"

  val twiceAsManyCats: Int => String = twice andThen countCats

  val numberOfCats = twiceAsManyCats(1)

  println(numberOfCats)
}
