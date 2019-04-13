package com.abc.tagless_final.shared

import shapeless.tag.@@

package object domain {

  sealed trait EmailAddress

  final case class User(email: String @@ EmailAddress, password: String)

}
