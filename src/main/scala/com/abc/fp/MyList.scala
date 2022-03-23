package com.abc.fp

import scala.annotation.tailrec

object MyList extends App {

  sealed abstract class SList[+T] {
    def head: T
    def tail: SList[T]
    def isEmpty: Boolean
    def toString: String
    def prepend[S >: T](s: S): SList[S]
//    def apply(i: Int): T
  }

  case object SLNil extends SList[Nothing] {
    override def head: Nothing = throw new NoSuchElementException
    override def tail: SList[Nothing] = throw new NoSuchElementException
    override def isEmpty: Boolean = throw new NoSuchElementException
    override def toString: String = "[]"
    override def prepend[S >: Nothing](s: S): SList[S] = Cons(s, SLNil)
  }

  case class Cons[+T](override val head: T, override val tail: SList[T]) extends SList[T] {
    override def isEmpty: Boolean = false

    override def toString: String = {
      @tailrec
      def toStringRec(remainder: SList[T], result: String): String = {
        if(remainder.isEmpty) result
        else if (remainder.tail.isEmpty) s"$result${remainder.head}"
        else toStringRec(remainder.tail, s"$result${remainder.head}, ")
      }

      toStringRec(this, "")
    }
    override def prepend[S >: T](s: S): SList[S] = Cons(s, this)
  }

}
