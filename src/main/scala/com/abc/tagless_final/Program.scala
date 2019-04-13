package com.abc.tagless_final

import cats.Monad
import com.abc.ProductId

class Program[M[_] : Monad](repo: ProductRepository[M]) {
//  def renameProduct(id: ProductId, name: String): M[Option[Product]] =
//    repo
//      .findProduct(productId = id)
//      .flatMap {
//        case Some(p) =>
//          val renamed = p.copy(name = name)
//          repo.saveProduct(renamed) map (_ => Some(renamed))
//        case None =>
//          Monad[M].pure(None)
//      }
}