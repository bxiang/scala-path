package com.abc

object ScalaIntro {

  type Funca = Int => String

  def funcA: Funca = i => {
    s"This is a number: $i"
  }

  def funcB(i: Int): String = {
    s"This is a number: $i"
  }

  def main(args: Array[String]): Unit = {
    println(funcA(1))
    println(funcB(1))

    val url = "[http://blaba.com/a/bc/d/756384576455]"
    val id = url.dropRight(1).reverse.takeWhile(_ != '/').reverse
    println(id)
  }

}
