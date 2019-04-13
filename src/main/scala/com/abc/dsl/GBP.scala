package com.abc.dsl

final case class GBP(value: BigDecimal)

object GBP {

  implicit object GBPIsAmount extends Amount[GBP] {
    override def zero: GBP = GBP(0)

    override def plus(a: GBP, b: GBP): GBP = GBP(a.value + b.value)

    override def times(a: GBP, b: BigDecimal): GBP = GBP(a.value * b)
  }

  implicit val prettyPrintGBP = PrettyPrint.instance[GBP](a => "£%.2f".format(a.value))

  implicit def prettyOps(a: GBP) = new prettyPrintGBP.Ops(a)

  implicit class GBPFromInt(value: Int) {
    def GBP: GBP = new GBP(value)
  }

  implicit def gbpAmountOps(a: GBP) = new GBPIsAmount.Ops(a)
}

