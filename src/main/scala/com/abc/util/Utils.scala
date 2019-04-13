package com.abc.util

import scala.io.Source
import scala.util.Try

object Utils {

  def elapseTime[T](blockName: String)(block: => T): T = {
    val start = System.nanoTime()
    val result = block // call by name
    val end = System.nanoTime()
    println(s"$blockName took ${end - start} ns")
    result
  }

  def using[A <: {def close() : Unit}, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }

  def readTextFile(filename: String): Try[List[String]] = {
    Try {
      using(Source.fromFile(filename)) {
        source => source.getLines.toList
      }
    }
  }

}
