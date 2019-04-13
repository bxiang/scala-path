package com.abc.dsl

final case class EUR(value: BigDecimal)

object EUR {

  implicit object EURIsAmount extends Amount[EUR] {
    override def zero: EUR = EUR(0)

    override def plus(a: EUR, b: EUR): EUR = EUR(a.value + b.value)

    override def times(a: EUR, b: BigDecimal) = EUR(a.value * b)
  }

  implicit val prettyPrintEUR = PrettyPrint.instance[EUR](a => "€%.2f".format(a.value))

  implicit def prettyOps(a: EUR) = new prettyPrintEUR.Ops(a)

  implicit class EURFromInt(value: Int) {
    def EUR = new EUR(value)
  }

  implicit def eurAmountOps(a: EUR) = new EURIsAmount.Ops(a)
}

