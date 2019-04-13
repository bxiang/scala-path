package com.abc.typeclasses

/**
  * see https://www.tobyhobson.co.uk/type-classes-for-beginners/
  */
object Typeclasses {

  import scala.reflect.ClassTag

  case class Car(make: String)

  case class Bus(colour: String)

  case class Tank(numGuns: Int)

  trait VehicleLike[A] {
    def drive(a: A): String
  }

  object VehicleLikeEvidences {

    implicit val vehicleLikeCar = new VehicleLike[Car] {
      override def drive(car: Car): String = s"driving a ${car.make} car"
    }

    implicit val vehicleLikeBus = new VehicleLike[Bus] {
      override def drive(bus: Bus): String = s"driving a ${bus.colour} bus"
    }

    implicit val vehicleLikeTank = new VehicleLike[Tank] {
      override def drive(tank: Tank): String = s"driving a tank with ${tank.numGuns} guns"
    }

    implicit def vehicleLikeList[A](implicit evidence: VehicleLike[A]) = new VehicleLike[List[A]] {
      override def drive(aa: List[A]): String = {
        s"List type class: ${System.lineSeparator()}" + aa.map(a => evidence.drive(a)).mkString(System.lineSeparator())
      }
    }

    /**
      * A variation on the List type class illustrating two new features:
      *
      * 1. Instead of specifying the evidence as an implicit parameter we use the A: VehicleLike and implicitly shortcuts
      * 2. We use another type class called ClassTag to get the type of A at runtime
      *
      * Note: A: VehicleLike : ClassTag means "any A so long as there are Vehicle and ClassTag type classes in scope"
      *
      * @tparam A
      * @return
      */
    implicit def vehicleLikeOption[A: VehicleLike : ClassTag] = new VehicleLike[Option[A]] {
      override def drive(aa: Option[A]): String = {
        val className = implicitly[ClassTag[A]].runtimeClass.getSimpleName
        aa.map(a =>
          s"Option[$className] type class: " + implicitly[VehicleLike[A]].drive(a)
        ).getOrElse(
          s"Option[$className] type class: Nothing to drive"
        )
      }
    }
  }

  def driveAndReturn[A](vehicle: A)(implicit evidence: VehicleLike[A]): A = {
    println(evidence.drive(vehicle));
    vehicle
  }

  def driveAndReturn2[A: VehicleLike](vehicle: A): A = {
    println(implicitly[VehicleLike[A]].drive(vehicle));
    vehicle
  }

  def main(args: Array[String]): Unit = {
    val cars = List(Car("ford"), Car("BMW"), Car("VM"))
    val busses = List(Bus("red"), Bus("yellow"))
    val tanks = List(Tank(1), Tank(2))

    import VehicleLikeEvidences._
    val drivenFord: Car = driveAndReturn(cars.head)
    val drivenCars: List[Car] = driveAndReturn(cars)

    val drivenBus: Bus = driveAndReturn(busses.head)
    val drivenBusses: List[Bus] = driveAndReturn(busses)

    val drivenTank: Tank = driveAndReturn(tanks.head)
    val drivenTanks: List[Tank] = driveAndReturn(tanks)

    val drivenOptionalTank: Option[Tank] = driveAndReturn(tanks.headOption)

    val drivenListOptionalTanks: List[Option[Tank]] = driveAndReturn(tanks.map(t => Some(t)) :+ None)

    val drivenOptionalListTanks: Option[List[Tank]] = driveAndReturn(Some(tanks): Option[List[Tank]])
  }

}