package com.abc.dsl

sealed abstract case class Percent(value: BigDecimal)

object Percent {

  implicit object PercentIsConstant extends Constant[Percent] {
    override def underlying(percent: Percent): BigDecimal = percent.value / 100
  }

  implicit def constantOps(a: Percent) = new PercentIsConstant.Ops(a)

  implicit val prettyPrintPercent = PrettyPrint.instance[Percent](percent => s"${percent.value}%")

  implicit def prettyOps(a: Percent) = new prettyPrintPercent.Ops(a)

  implicit class PercentFromInt(value: Int) {
    def percent: Percent = new Percent(value) {}
  }

}


