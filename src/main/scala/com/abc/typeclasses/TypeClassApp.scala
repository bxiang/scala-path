package com.abc.typeclasses

/**
  * see http://debasishg.blogspot.com/2010/06/scala-implicits-type-classes-here-i.html
  */
object TypeClassApp extends App {

  case class Address(no: Int, street: String, city: String, state: String, zip: String)

  trait LabelLike[T] {
    def toLabel(value: T): String
  }

  object LabelLikeInstances {

    //    implicit object AddressLabelMaker extends LabelLike[Address] {  // you can define it this way too
    implicit val addressLabelMaker = new LabelLike[Address] {
      def toLabel(address: Address): String = {
        import address._
        "%d %s, %s, %s - %s".format(no, street, city, state, zip)
      }
    }

  }

  def printLabel[T](t: T)(implicit lm: LabelLike[T]) = lm.toLabel(t)

  def printLabel2[T: LabelLike](t: T) = implicitly[LabelLike[T]].toLabel(t)

  import LabelLikeInstances._

  val label = printLabel2(Address(100, "Monroe Street", "Denver", "CO", "80231"))
  println(label)

}
