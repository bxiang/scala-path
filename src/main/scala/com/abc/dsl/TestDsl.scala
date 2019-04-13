package com.abc.dsl

import Amount._
import Constant._
import EUR._
import GBP._
import Percent._
import Period._

object TestDsl extends App {

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
