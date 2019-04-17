package com.abc.cats

import cats.Functor

object CatsApp extends App {

  case class LineItem(price: Double)

  import cats.instances.list._

  def withTax[F[_]](order: F[LineItem])(implicit ev: Functor[F]): F[LineItem] = {
    Functor[F].map(order)(o => o.copy(price = o.price * 1.13))
  }

  val lineItems = List(LineItem(10.0), LineItem(20.0))

  withTax(lineItems).foreach(println)


  import cats.syntax.functor._

  def withTax2[F[_] : Functor](order: F[LineItem]): F[LineItem] = {
    order.map(o => o.copy(price = o.price * 1.15))
  }

  withTax2(lineItems).foreach(println)


  import cats.Monoid
  import cats.implicits._

  val list = List(1, 2, 3)
  val x = Monoid.combineAll(list)
  println(x)
}
