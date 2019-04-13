package com.abc

// Phantom types
final case class ProductId(val id: String) extends AnyVal

//def addToCart(cartId: CartId, productId: ProductId, providerId: ProviderId)

case class Id[A](val id: String) extends AnyVal

sealed trait Cart
sealed trait Product
sealed trait Provider

// def addToCart(cartId: Id[Cart], productId: Id[Product],  providerId: Id[Provider])




