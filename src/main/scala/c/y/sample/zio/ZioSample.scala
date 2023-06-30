package c.y.sample.zio

import zio._

import java.io.IOException

object ZioSample extends ZIOAppDefault {

  trait A
  trait B
  trait C

  case class CLive(a: A, b: B) extends C

  object CLive {
    val layer: ZLayer[A & B, Nothing, C] =
      ZLayer.fromFunction(CLive.apply _)
  }

//  object CLive {
//    val layer: ZLayer[A & B, Nothing, C] =
//      ZLayer {
//        for {
//          a <- ZIO.service[A]
//          b <- ZIO.service[B]
//        } yield CLive(a, b)
//      }
//  }

//  val myApp = ZIO.succeed[CLive] (ZIO.
  val zipRight1: ZIO[Any, IOException, String] =
    Console.printLine("What is your name?").zipRight(Console.readLine)
  val zipRight2: ZIO[Any, IOException, String] =
    Console.printLine("What is your name?") *>
      Console.readLine
  val zeither: ZIO[Any, Nothing, Either[String, Nothing]] =
    ZIO.fail("Uh oh!").either

  def openFile(str: String) : ZIO[Any, IOException, Array[Byte]] =
    ZIO.fail(new IOException("Failed"))

  val primaryOrSecondaryData: ZIO[Any, IOException, Array[Byte]] =
    openFile("primary.data").foldZIO(
      _ => openFile("secondary.data"), // Error handler
      data => ZIO.succeed(data)) // Success handler

  lazy val DefaultData: Array[Byte] = Array(0, 0)
  val primaryOrDefaultData: ZIO[Any, Nothing, Array[Byte]] =
    openFile("primary.data").fold(
      _ => DefaultData, // Failure case
      data => data) // Success case

  val app = for {
//    rl <- zeither
//    _ <- Console.printLine("r-l =>" + rl)
//    res <- primaryOrSecondaryData
    res <- primaryOrDefaultData
    _ <- Console.printLine("res = " + res)
  } yield()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = app.exitCode
}
