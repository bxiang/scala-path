package com.abc.dsl

object Period {

  sealed trait Month

  sealed trait Year

  final val Month = new Month() {}
  final val Year = new Year() {}

  implicit val prettyPrintMonth = PrettyPrint.instance[Month](_ => "Month")
  implicit val prettyPrintYear = PrettyPrint.instance[Year](_ => "Year")

  sealed trait Period[B] {
    def instance: B

    def changePeriod[A: Amount, C: Period](a: A Per B, period: C): A Per C =
      if (period == a.period) Per(a.value, period)
      else if (period == Month) Per(implicitly[Amount[A]].times(a.value, BigDecimal(1) / 12), period)
      else Per(implicitly[Amount[A]].times(a.value, 12), period)

    class Ops[A: Amount](a: A Per B) {
      def convertTo[C: Period](c: C): A Per C = changePeriod(a, c)
    }

  }

  implicit object MonthIsPeriod extends Period[Month] {
    override def instance: Month = Month
  }

  implicit object YearIsPeriod extends Period[Year] {
    override def instance: Year = Year
  }

  final case class Per[A, B: Period](value: A, period: B)

  implicit class PerFromValue[A](value: A) {
    def per[B: Period](period: B): A Per B = Per(value, period)
  }

  implicit def prettyPrintPer[A: PrettyPrint, B: PrettyPrint]: PrettyPrint[A Per B] = {
    val prettyA = implicitly[PrettyPrint[A]]
    val prettyB = implicitly[PrettyPrint[B]]
    PrettyPrint.instance[A Per B](a => s"${prettyA.prettify(a.value)} per ${prettyB.prettify(a.period)}")
  }

  implicit def prettyOps[A, B](a: A Per B)(implicit prettyPrint: PrettyPrint[A Per B]) = new prettyPrint.Ops(a)

  implicit def perIsAmount[A: Amount, B: Period]: Amount[A Per B] = new Amount[A Per B] {
    val amount = implicitly[Amount[A]]
    val period = implicitly[Period[B]]

    override def zero = Per(amount.zero, period.instance)

    override def plus(a: A Per B, b: A Per B): A Per B = Per(amount.plus(a.value, b.value), period.instance)

    override def times(a: A Per B, b: BigDecimal): A Per B = Per(amount.times(a.value, b), period.instance)
  }

  implicit def perAmountOps[A, B](a: A Per B)(implicit amount: Amount[A Per B]) = new amount.Ops(a)

  implicit def perPeriodOps[A, B](a: A Per B)(implicit amount: Amount[A], period: Period[B]) = new period.Ops(a)

  implicit class PerMonthOrYearSequence[A: Amount](seq: Seq[A Per _]) {

    def unifyPer[C: Period](c: C): Seq[A Per C] =
      seq.map { a =>
        if (a.period == Month) a.asInstanceOf[A Per Month] convertTo c
        else a.asInstanceOf[A Per Year] convertTo c
      }
  }

}
