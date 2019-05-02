package Leet

import org.scalatest.FunSuite

// see@ https://leetcode.com/problems/best-time-to-buy-and-sell-stock/
class Leet121 extends FunSuite {

  def maxProfit(prices: Seq[Int]): Int = {
    prices match {
      case Nil => 0
      case x :: xs =>
        (xs.foldLeft((0, x))((accu, price) => {
          val (oldProfit, lowestPrice) = accu
          val newProfit = price - lowestPrice
          val highestProfit = if (newProfit > oldProfit) newProfit else oldProfit
          val newLowestPrice = if (price > lowestPrice ) lowestPrice else price
          (highestProfit, newLowestPrice)
        }))._1
    }
  }

  test("maxProfit") {
    assert(maxProfit(List(1, 2, 3, 4, 5, 6)) == 5)
    assert(maxProfit(List(6, 5, 4, 3, 2, 1)) == 0)
    assert(maxProfit(List(1, 1, 1, 1, 1, 1)) == 0)
    assert(maxProfit(List(1, 2, 3, 4, 2, 2)) == 3)
    assert(maxProfit(List(3, 4, 2, 2, 1, 0)) == 1)
    assert(maxProfit(List(1, 2, 3, 2, 4, 5, 6)) == 5)
    assert(maxProfit(List(1, 2, 3, 4, 5, 2, 3)) == 4)
    assert(maxProfit(List(8, 4, 1, 2, 3, 4, 5)) == 4)
    assert(maxProfit(List(8, 4, 1, 2, 3, 1, -1)) == 2)
  }

}