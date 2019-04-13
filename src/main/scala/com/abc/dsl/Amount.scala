package com.abc.dsl

trait Amount[A] {
  def zero: A

  def plus(a: A, b: A): A

  def times(a: A, b: BigDecimal): A

  class Ops(a: A) {
    def +(b: A) = plus(a, b)

    def -(b: A) = plus(a, times(b, -1))

    def *[B](b: B)(implicit constant: Constant[B]): A = times(a, constant.underlying(b))

    def /[B](b: B)(implicit constant: Constant[B]): A = times(a, BigDecimal(1) / constant.underlying(b))
  }

  class SeqOps(seq: Seq[A]) {
    // sum might be more appropriate however it is already define on TraversableLike (which Seq inherits from)
    // and the TraversableLike sum will be used without giving a chance to resolve this one
    def total: A = seq.foldLeft(zero)(plus)
  }

}

object Amount {
  implicit def seqAmountOps[A](seq: Seq[A])(implicit amount: Amount[A]) = new amount.SeqOps(seq)
}
