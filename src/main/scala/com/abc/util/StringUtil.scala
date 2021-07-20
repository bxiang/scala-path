package com.abc.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object StringUtil extends App {

  implicit class EnrichedString(s: String) {

    import scala.util.Try

    lazy val dateReportedDTF: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")

    def toIntOpt = Try(s.toInt) toOption

    def reportedDate = LocalDateTime.parse(s, dateReportedDTF).toLocalDate

  }

  val x = "12".toIntOpt

  println(x)
}
