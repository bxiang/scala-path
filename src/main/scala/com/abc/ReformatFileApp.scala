package com.abc

import java.io.{File, PrintWriter}

import scala.io.Source

/**
  * @author ${user.name}
  */
object ReformatFileApp {

  def composeNewLine(line: Array[String]): String = {
    s"${line(0)},${line(1)},${line(2)},${line(3)},${line(4)},,,${line(5)},${line(6)},${line(7)}\n"
  }

  def main(args: Array[String]): Unit = {
    val baseDir = if (args == null || args.isEmpty || args(0) == null) "." else args(0)
    new File(baseDir + "/src/main/resources/mapping").listFiles.map(_.getName).foreach {
      fileName =>
        val lines = Source.fromFile(baseDir + "/src/main/resources/mapping/" + fileName).getLines.map(_.split(",")).toList
        val writer = new PrintWriter(new File(baseDir + "/src/main/resources/mapping/" + fileName))
        lines.foreach {
          line =>
            writer.write(composeNewLine(line))
        }
        writer.close
    }
  }

}

