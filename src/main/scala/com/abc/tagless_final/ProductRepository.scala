package com.abc.tagless_final

import com.abc.{Product, ProductId}

trait ProductRepository[M[_]] {
  def findProduct(productId: ProductId): M[Option[Product]]

  def saveProduct(product: Product): M[Unit]

  def incrementProductSells(productId: ProductId, quantity: Int): M[Unit]
}
