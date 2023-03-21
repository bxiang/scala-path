package com.abc.typelevel

object TypeLevelProgramming {
  
    import scala.reflect.runtime.universe._

    def show[T](value: T)(implicit tag: TypeTag[T]) = tag.toString().replace("com.abc.typelevel", "")

    def main(args: Array[String]): Unit = {
        println(show(List(1,2,3)))
    }

}
