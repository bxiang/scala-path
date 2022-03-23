package com.abc

package object typeclasses {

  sealed trait Animal
  final case class Dog(name: String) extends Animal
  final case class Cat(name: String) extends Animal
  final case class Bird(name: String) extends Animal

  trait BehavesLikeHuman[A] {
    def speak(a: A): Unit
  }

  object BehavesLikeHumanInstances {
    implicit val dogBehavesLikeHuman = new BehavesLikeHuman[Dog] {
        def speak(dog: Dog): Unit = {
            println(s"I'm a Dog, my name is ${dog.name}")
        }
    }
  }

  object BehavesLikeHumanSyntax {
    implicit class BehavesLikeHumanOps[A](value: A) {
        def speak(implicit behavesLikeHumanInstance: BehavesLikeHuman[A]): Unit = {
            behavesLikeHumanInstance.speak(value)
        }
    }}

  object HumanLikeApp extends App {

    import com.abc.typeclasses.BehavesLikeHumanInstances.dogBehavesLikeHuman
    import com.abc.typeclasses.BehavesLikeHumanSyntax.BehavesLikeHumanOps

    val thisDog = Dog("Butters")

    thisDog.speak

  }
  
}
