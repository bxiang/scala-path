
val squareRoot: PartialFunction[Double, Double] = {
  case d: Double if d > 0 => Math.sqrt(d)
}

val list: List[Double] = List(4, 16, 25, -9)

val result1 = list.map(Math.sqrt)
result1.foreach(println)

val result2 = list.collect(squareRoot)
result2.foreach(println)

val pf: PartialFunction[Int, String] = {
  case i if i % 2 == 0 => "even"
}

val tf: (Int => String) = pf orElse { case _ => "odd" }

val x1 = tf(1)
val x2 = tf(2)