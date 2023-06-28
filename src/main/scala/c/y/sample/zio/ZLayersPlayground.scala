package c.y.sample.zio

import zio._
import zio.Console._

object ZLayersPlayground extends zio.ZIOAppDefault {

  val meaningOfLife = ZIO.succeed(42)
  val aFailure = ZIO.fail("Something went wrong")

  val greeting = for {
    _ <- Console.printLine("Hi, what's your name?")
    name <- readLine
    _ <- printLine(s"Hello! $name")
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    greeting.exitCode

  /** impl service  **/

  case class User(name: String, email: String)

  object UserEmailer {
    trait Service {
      def notify(user: User, message: String): Task[Unit]
    }

    val live: ZLayer[Any, Nothing, UserEmailer.Service] = ZLayer.succeed(new Service {
      override def notify(user: User, message: String): Task[Unit] =
        ZIO.succeed(println(s"Sending '$message' to ${user.email}"))
    })

    //    def notify(user: User, message: String): ZIO[UserEmailer.Service, Throwable, Unit] =
    //      ZIO.access
  }
}
