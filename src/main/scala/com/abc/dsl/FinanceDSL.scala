package com.abc.dsl


object FinanceDSL extends App {

  trait PrettyPrint[A] {
    def prettify(a: A): String

    class Ops(a: A) {
      def pretty: String = prettify(a)
    }

  }

  object PrettyPrint {
    def instance[A](pretty: A => String) = new PrettyPrint[A] {
      override def prettify(a: A): String = pretty(a)
    }
  }

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

  final case class GBP(value: BigDecimal)

  final case class EUR(value: BigDecimal)

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


  import Amount._
  import Constant._
  import EUR._
  import GBP._
  import Percent._
  import Period._
  import PrettyPrint._

  val tenEuros = 10.EUR
  val tenPounds = 10.GBP
  val onePounds = 10.percent * 10.GBP
  val twoPounds = 10.GBP * 20.percent
  val threePounds = 3 * 1.GBP
  val fourPounds = 2.GBP * 2
  val fivePounds = 2.GBP + 3.GBP

  println(fivePounds.pretty)
  //  val constantAndPounds = 1 + 2.GBP // doesn't compile: can't add constant and pounds
  val fiftyPounds = List(10.GBP, 15.GBP, 25.GBP).total
  //  val eurosAndPounds = tenEuros + tenPounds // doesn't compile: can't add eur and pounds
  //  val totalEurosAndPounds = List(10.GBP, 10.EUR).total // doesn't compile: can't add eur and pounds
  val totalIncome = List(1000.EUR per Month, 2000.EUR per Month).total
  println(totalIncome.pretty)
  //  val mixedIncome = List(1000.EUR per month, 12000.EUR per year).total // doesn't compile: can't sum year and month
  val convertedIncome = List(1000.EUR per Month, 18000.EUR per Year).unifyPer(Month).total
  println(convertedIncome.pretty)
  val raise = convertedIncome * 10.percent
  println(raise.pretty)
  val summedIncome = (1000.EUR per Month) + (2000.EUR per Month)
  val convertedSummedIncome = summedIncome.convertTo(Year)
  println(s"${summedIncome.pretty} -> ${convertedSummedIncome.pretty}")
  //  val squarePounds = 2.GBP * 3.GBP // doesn't compile

}