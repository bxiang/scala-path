package com.abc.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object StringUtil {

  implicit class EnrichedString(val s: String) {

    import scala.util.Try

    lazy val dateReportedDTF: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")

    def toIntOpt = Try(s.toInt) toOption

    def reportedDate = LocalDateTime.parse(s, dateReportedDTF).toLocalDate



  }

}
