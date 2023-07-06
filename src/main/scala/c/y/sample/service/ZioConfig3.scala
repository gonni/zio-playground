package c.y.sample.service

import zio._
import java.io.File

import zio._
import zio.config._
import zio.config.typesafe._
import java.io.IOException
import zio.config.magnolia.deriveConfig

case class ServiceConfig(
                          influxUrl: String,
                          influxAuthToken: String,
                          influxOrg: String,
                          kafkaHosts: List[String],
                        )
object ServiceConfig {
  val config: Config[ServiceConfig] = deriveConfig[ServiceConfig]
}

object ZioConfig3 extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setConfigProvider(
      TypesafeConfigProvider
        .fromResourcePath()
    )

  val serverConfig: ZLayer[Any, Config.Error, ServiceConfig] =
    ZLayer
      .fromZIO(
        ZIO.config[ServiceConfig](ServiceConfig.config).map { c =>
          ServiceConfig(
            c.influxUrl, c.influxAuthToken, c.influxOrg, c.kafkaHosts
          )
        }
      )


  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val a = for {
      conf <- ZIO.service[ServiceConfig]
      _ <- Console.printLine(s"CONF = $conf")
    } yield()

    a.provide(serverConfig)
  }

}
