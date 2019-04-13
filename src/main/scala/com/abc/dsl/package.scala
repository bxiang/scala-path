package com.abc

package object dsl {

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

}
