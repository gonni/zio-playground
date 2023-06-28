package c.y.sample.zio

import zio._

object ZioErrorsMain extends ZIOAppDefault {

  def validateNonNegativeNumber(input: String): ZIO[Any, String, Int] =
    input.toIntOption match {
      case Some(value) if value >= 0 =>
        ZIO.succeed(value)
      case Some(other) =>
        ZIO.fail(s"the entered number is negative: $other")
      case None =>
        ZIO.die(
          new NumberFormatException(
            s"the entered input is not in the correct number format: $input"
          )
        )
    }

  val myApp: ZIO[Any, String, Int] =
    for {
      f <- validateNonNegativeNumber("-10").fork
      _ <- f.interrupt
      r <- f.join
    } yield r

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    myApp.map(println)
}
