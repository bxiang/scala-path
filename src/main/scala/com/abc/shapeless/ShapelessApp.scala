package com.abc.shapeless

import java.time.LocalDate
import shapeless._
import shapeless.syntax.singleton._

object ShapelessApp extends App {

  case class Location(pickup: String, dropOff: String, from: LocalDate, to: LocalDate)

  case class Vehicle(vehicleCategory: String, automatic: Boolean, numDoors: Int)

  case class Driver(driverAge: Int, nationality: String)

  case class Reservation(to: LocalDate, from: LocalDate, pickup: String, dropOff: String, vehicleCategory: String,
                         automatic: Boolean, numDoors: Int, driverAge: Int, nationality: String, reservationConfirmed: Boolean)

  //  type LocationH = String :: String :: LocalDate :: LocalDate :: HNil
  //  type VehicleH = String :: Boolean :: Int :: HNil
  //  type DriverH = Int :: String :: HNil
  //  type ReservationH = String :: String :: LocalDate :: LocalDate :: String :: Boolean :: Int :: Int :: String :: Boolean :: HNil
  //  val locationH: LocationH = "Malaga Airport" :: "Malaga Airport" :: LocalDate.of(2018, 8, 1) :: LocalDate.of(2018, 8, 10) :: HNil
  //  val vehicleH: VehicleH = "Economy" :: false :: 4 :: HNil
  //  val driverH: DriverH = 35 :: "British" :: HNil
  //  val reservationH: ReservationH = locationH ++ vehicleH ++ driverH :+ false
  //  println(reservationH)

  val location = Location("Malaga Airport", "Pearson Airport", LocalDate.of(2018, 8, 1), LocalDate.of(2018, 8, 10))
  val vehicle = Vehicle("Economy", false, 4)
  val driver = Driver(35, "British")

  val locationH1 = LabelledGeneric[Location].to(location)
  val vehicleH1 = LabelledGeneric[Vehicle].to(vehicle)
  val driverH1 = LabelledGeneric[Driver].to(driver)
  val reservationGen = LabelledGeneric[Reservation]

  val reservationH1 = locationH1 ++ vehicleH1 ++ driverH1 :+ ('reservationConfirmed ->> false)

  val reservation1: Reservation = LabelledGeneric[Reservation].from(reservationH1.align[reservationGen.Repr])

  println(location)
  println(reservation1)

}
