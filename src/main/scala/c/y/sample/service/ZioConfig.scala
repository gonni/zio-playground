package c.y.sample.service

import com.typesafe.config.ConfigFactory
import zio._
import scala.jdk.CollectionConverters._

object ZioConfig extends ZIOAppDefault {

  case class ServiceConfig(
                            influxUrl: String,
                            influxAuthToken: String,
                            influxOrg: String,
                            kafkaHosts: List[String],
                            )
  object ServiceConfig {
    val layer2 = ZLayer.succeed(
      ServiceConfig(influxUrl = RtConfig().getStringValue("influx.url"),
        influxAuthToken = RtConfig().getStringValue("influx.authToken"),
        influxOrg = RtConfig().getStringValue("influx.org"),
        kafkaHosts = RtConfig().getArrayValue("kafka.urls").asScala.map(a => String.valueOf(a)).toList
      )
    )
  }

  val app = for {
    config <- ZIO.service[ServiceConfig]
    _ <- Console.printLine("RuntimeConfig -> " + config)
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    app.provide(ServiceConfig.layer2)
}

case class RtConfig(filePath: String = "application.conf") {
  val conf = ConfigFactory.load(filePath)
  def getRuntimeConfig() = conf.getConfig(conf.getString("profile.active"))

  def getStringValue(pathKey: String) = getRuntimeConfig().getString(pathKey)
  def getArrayValue(pathKey: String) = getRuntimeConfig().getAnyRefList(pathKey)
}

//object RuntimeConfig {
//  val conf = ConfigFactory.load("application.conf")
//  var env: Option[String] = None
//
//  def getRuntimeConfig() = {
//    conf.getConfig(env.getOrElse(conf.getString("profile.active")))
//    //    conf.getConfig(conf.getString("profile.active"))
//  }
//
//  def apply(key: String) = getRuntimeConfig().getString(key)
//
//}
