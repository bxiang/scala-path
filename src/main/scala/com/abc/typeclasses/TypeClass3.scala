object TypeClass3 extends App {


trait SomeTypeClassForType[A] {
    def someMethod(a: A): Int
}

def algorithm[A](a: A)(using instance: SomeTypeClassForType[A]): Unit = 
    println(instance.someMethod(a))

given SomeTypeClassForType[String] with
    def someMethod(a: String): Int = a.length


algorithm("foo")    

}