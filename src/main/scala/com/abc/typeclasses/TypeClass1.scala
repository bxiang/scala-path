package com.abc.typeclasses

object TypeClass1 extends App {

  trait Behavior[A] {
    def area(a: A): Double
  }

  case class Circle(radius: Double)
  case class Rectangle(width: Double, length: Double)

  implicit object CircleBehavior extends Behavior[Circle] {
    override def area(circle: Circle) : Double = math.Pi * math.pow(circle.radius, 2)
  }

  implicit object RectangleShape extends Behavior[Rectangle] {
    override def area(rectangle: Rectangle): Double = rectangle.width * rectangle.length
  }

  implicit class ShapeOp[A](s: A) {
    def area(implicit ev: Behavior[A]): Double = ev.area(s)
  }

//  def areaOf[A](shape: A)(implicit shapeImpl: Shape[A]): Double = shapeImpl.area(shape)
//
//  areaOf(Circle(10))
//  areaOf(Rectangle(5,5))

  val c = Circle(10)
  val r = Rectangle(5, 5)

  println(c.area)
  print(r.area)
}
