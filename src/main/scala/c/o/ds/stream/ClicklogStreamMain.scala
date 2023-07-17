package c.o.ds.stream

import zio._
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object ClicklogStreamMain extends ZIOAppDefault {
  override val bootstrap = {
    Runtime.removeDefaultLoggers ++ SLF4J.slf4j(LogLevel.Info, LogFormat.colored)
  }

//  val myApp = for {
//    _ <- Console.printLine("Start System")
//    - <- ZIO.log("Start System at _________ " + System.OS)
//    conf <- SvcConfigZio.zioEnvSvcConf()
//    _ <- Console.printLine("Conf ->" + conf)
//  } yield()

  val streamingApp = KafkaZio.myConsumer2
    .runDrain
    .provide(
      SvcConfigZio.layer, KafkaZio.layer, InfluxClientZio.layer
    )

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    streamingApp.exitCode
  }
}
