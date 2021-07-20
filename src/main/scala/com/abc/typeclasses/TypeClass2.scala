package com.abc.typeclasses

object TypeClass2 extends App {

  case class Dog(name: String)
  case class Cat(name: String, color: String)

  trait CanSpeak[A] {
    def speak(a: A): Unit
  }

  implicit object DogSpeak extends CanSpeak[Dog] {
    override def speak(a: Dog) = println(s"I am a Dog. my name is ${a.name}")
  }

  implicit object CatSpeak extends CanSpeak[Cat] {
    override def speak(a: Cat): Unit = println(s"This is ${a.name}. I am a ${a.color} cat")
  }

  implicit class Op[A](a: A) {
    def speak(implicit ev: CanSpeak[A]): Unit = ev.speak(a)
  }

  val a1 = Dog("Butters")
  val a2 = Cat("Sam", "Black")

  a1.speak
  a2.speak

}
