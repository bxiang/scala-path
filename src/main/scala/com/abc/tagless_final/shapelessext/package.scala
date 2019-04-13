package com.abc.tagless_final

import cats.Eq
import shapeless.Unwrapped
import shapeless.tag.@@

package object shapelessext {

  implicit def tagEqual[A : Eq, B]: Eq[A @@ B] =
    Eq.by(a => Unwrapped[A].unwrap(a))

}
