package c.o.ds.stream

import com.typesafe.config.ConfigFactory
import scala.jdk.CollectionConverters._
import zio._

object SvcConfigZio {
  case class ServiceConfig(
                            influxUrl: String,
                            influxAuthToken: String,
                            influxOrg: String,
                            kafkaHosts: List[String],
                            topic: String
                          )

  case class RtConfig(filePath: String = "application.conf") {
    val conf = ConfigFactory.load(filePath)
    def getRuntimeConfig() = conf.getConfig(conf.getString("profile.active"))
    def getStringValue(pathKey: String) = getRuntimeConfig().getString(pathKey)
    def getArrayValue(pathKey: String) = getRuntimeConfig().getAnyRefList(pathKey)
  }

  def loadConfig(filePath: String = "application.conf") = {
    val cfg = RtConfig(filePath)
    val res = ServiceConfig(influxUrl = cfg.getStringValue("influx.url"),
      influxAuthToken = cfg.getStringValue("influx.authToken"),
      influxOrg = cfg.getStringValue("influx.org"),
      kafkaHosts = cfg.getArrayValue("kafka.urls").asScala.map(a => String.valueOf(a)).toList,
      topic = cfg.getStringValue("kafka.topic")
    )

    println(s"Loaded LOG with $filePath -->" + res)
    res
  }

  def zioSvcConf(file: String = "application.conf") =
    ZIO.succeed(loadConfig(file))

  def zioEnvSvcConf() = for {
    f <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("application.conf"))
    _ <- Console.printLine("Init CFG file :" + f).orElse(ZIO.succeed())
    //cfgFile <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("application.conf"))
  } yield loadConfig(f)

  val layer: TaskLayer[ServiceConfig] = ZLayer.fromZIO(zioEnvSvcConf())
}
