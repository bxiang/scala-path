package com.abc

object Match extends App {

  val xs = 3 :: 6 :: 12 :: 24 :: Nil

  val result = xs match {
//    case x :: xs   match head
    case List(_*) :+ 24 => "end with 24" // match the last element
    case List(a, b, _*) => a * b // match first 2 elements
    case _ => 0
  }

  println(result)

  val l1 = List(1, 2, 3, 4, 5)

  val sum = l1.fold(0)(_ + _)
  println(sum)
  val sum2 = l1.foldLeft("")((x, y) =>  x+y)
  println(sum2)


  val s = "abcd"

  def doStuff(input: List[Char]): List[List[Char]] = {
    input match {
      case Nil => List.empty
      case x :: Nil => List(List(x.toUpper), List(x.toLower))
      case x :: xs =>
        doStuff(xs).flatMap(s =>
          {
            List(x.toUpper, x.toLower).map { y => y :: s}
          }
        )
//        for {
//          s <- doStuff(xs)
//          c <- List(x.toUpper, x.toLower)
//        } yield c :: s
    }
  }

  def findString(input: String): List[String] = doStuff(input.toList).map(_.mkString)

  val res = findString(s)
  println(res)

}
