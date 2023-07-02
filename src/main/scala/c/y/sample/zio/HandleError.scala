package c.y.sample.zio

import zio._

import java.io.{FileNotFoundException, IOException}

object HandleError extends ZIOAppDefault {

  val zeither: ZIO[Any, Nothing, Either[String, Nothing]] = {
    ZIO.fail("Uh oh!").either
  }

  val zeApp = for {
    z <- zeither
    _ <- Console.printLine("Res ->" + z)
  } yield ()
  // ------

  def openFile(str: String): ZIO[Any, IOException, Array[Byte]] = {
    ZIO.fail(new FileNotFoundException("Invalid File"))
//    ZIO.succeed(Array[Byte](1, 2))
  }

  val z: ZIO[Any, IOException, Array[Byte]] =
    openFile("primary.json").catchAll { error =>
      for {
        _ <- ZIO.logErrorCause("Could not open primary file", Cause.fail(error))
        file <- openFile("backup.json")
      } yield file
    }

  val data: ZIO[Any, IOException, Array[Byte]] =
    openFile("primary.data").catchSome {
      case _: FileNotFoundException =>
        openFile("backup.data")
    }

  // ------

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = data

}
