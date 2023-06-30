package c.y.sample.zio

import zio._

class Editor(formatter: Formatter, compiler: Compiler) {
  // ...
}

class Compiler() {
  // ...
}

class Formatter() {
  // ...
}

object InjecttionSample extends ZIOAppDefault {

  object Formatter {
    val layer: ZLayer[Any, Nothing, Formatter] =
      ZLayer.succeed(new Formatter())
  }

  object Compiler {
    val layer: ZLayer[Any, Nothing, Compiler] =
      ZLayer.succeed(new Compiler())
  }

  object Editor {
    val layer: ZLayer[Formatter with Compiler, Nothing, Editor] =
      ZLayer {
        for {
          formatter <- ZIO.service[Formatter]
          compiler <- ZIO.service[Compiler]
        } yield new Editor(formatter, compiler)
      }
  }

  case class Counter(ref: Ref[Int]) {
    def inc: UIO[Unit] = ref.update(_ + 1)
    def dec: UIO[Unit] = ref.update(_ - 1)
    def get: UIO[Int] = ref.get
    def test: UIO[Int] = ZIO.succeed(1)
  }

  val editor: ZLayer[Formatter with Compiler, Nothing, Editor] =
    (Formatter.layer ++ Compiler.layer) >>> Editor.layer

//  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
//    ZIO.succeed(editor).exitCode

  // Scoped
  case class A(a: Int)

  object A {
    val layer: ZLayer[Any, Nothing, A] =
      ZLayer.scoped {
        ZIO.acquireRelease(acquire = ZIO.debug("Initializing A") *> ZIO.succeed(A(5)))(
          release = _ => ZIO.debug("Releasing A")
        )
      }
  }

  val myApp: ZIO[A, Nothing, Int] =
    for {
      a <- ZIO.serviceWith[A](_.a)
    } yield a * a

  def run =
    myApp
      .debug("result")
      .provide(A.layer)

}
