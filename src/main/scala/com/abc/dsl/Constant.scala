package com.abc.dsl

trait Constant[B] {
  def underlying(b: B): BigDecimal

  class Ops(a: B) {
    def *[A](b: A)(implicit amount: Amount[A]) = amount.times(b, underlying(a))
  }

}

object Constant {

  implicit object IntIsConstant extends Constant[Int] {
    override def underlying(b: Int) = BigDecimal(b)
  }

  implicit def intConstantOps(b: Int) = new IntIsConstant.Ops(b)
}
