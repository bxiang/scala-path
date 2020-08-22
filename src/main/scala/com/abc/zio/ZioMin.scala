package com.abc.zio

import zio._

object GitHub {
    val live: Zlayer[Any, Nothing, Has[GitHub]] = 
        ZLayer.succeed(new Github())
}

class GitHub() {
    
    def open(file: String): Managed[IOException, Source] = {
        val acquire = ZIO(unsafeOpen(file)).refineToOrDie[IOException]
        val release = (source: Source) => ZIO(source.close()).orDie
        Managed.make(acquire)(release)
    }

    def download(file: String): ZIO[Any, IOException, String] = {
        open(file).use { source => 
            ZIO(source.getLines().mkString("\n")).refineToOrDie[IOException]
        }
    }

}

trait Covid19 {

    def load(day: Int, month: Int)

}

object Covid19 {

    class Live(github: Github) extends Covid19 {

        def load(day: Int, month: Int): ZIO[Any, IOException, Csv] = 
            for {
                string <- github.downlaod(formFileanme(day, month))
            } yield Csv.fromString(string)

    }

    val live: ZLayer[Has[GitHub], Nothing, Has[Covid19]] = 
        ZLayer.fromFunction[Has[GitHub], Covid19](env => new Live(env.get))

    def load(day: Int, month: Int): ZIO[Has[Covid19], IOException, Csv] =
     ZIO.accessM(_.get[Covid19].load(day, month))

}

def main(args: Array[String]): Unit = {

    val app: ZIO[Has[Covid19] with zio.console.Console] = ???

    val customLayer = GitHub.live >>> Covid19.live

    app.provideCustomerLayer(customLayer)

}