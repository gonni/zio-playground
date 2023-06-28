package c.y.sample.zio

import zio._

object ZlayerCreationMain extends ZIOAppDefault {

  // 1.
  case class AppConfig(host: String, port: Int)
  val configLayer: ULayer[AppConfig] = ZLayer.succeed(AppConfig("localhost", 8080))

  // 2.
  trait EmailService {
    def send(email: String, content: String): UIO[Unit]
  }

  object EmailService {
    val layer: ZLayer[Any, Nothing, EmailService] =
      ZLayer.succeed(
        new EmailService {
          override def send(email: String, content: String): UIO[Unit] = ???
        }
      )
  }

  // 3.
  trait A
  trait B
  trait C
  case class CLive(a: A, b: B) extends C

//  object CLive {
//    val layer: ZLayer[A & B, Nothing, C] =
//      ZLayer {
//        for {
//          a <- ZIO.service[A]
//          b <- ZIO.service[B]
//        } yield CLive(a, b)
//      }
//  }

  // 4.
  object CLive {
    val layer: ZLayer[A & B, Nothing, C] =
      ZLayer.fromFunction(CLive.apply _)
  }




  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = ???
}
