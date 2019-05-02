package com.abc.util

import java.io.{File, PrintWriter}

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

  def writeTextFile(filename: String, content: List[String]) = {
    val writer = new PrintWriter(new File(filename))
    content.foreach {
      line =>
        writer.write(s"${line}\n")
    }
    writer.close
  }

  import cats.Monoid
  import cats.implicits._

  def optionMonoid[T: Monoid] = new Monoid[Option[T]] {
    override def empty: Option[T] = None

    override def combine(a: Option[T], b: Option[T]) = for {
      t1 <- a
      t2 <- b
    } yield t1.combine(t2)

  }

}