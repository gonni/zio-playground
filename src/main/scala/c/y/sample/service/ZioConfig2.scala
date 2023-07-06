package c.y.sample.service

//import c.y.sample.service.ZioConfig.ServiceConfig
//import com.typesafe.config.ConfigFactory
import zio._
import scala.jdk.CollectionConverters._

object ZioConfig2 extends ZIOAppDefault {

  case class ServiceConfig(
                            influxUrl: String,
                            influxAuthToken: String,
                            influxOrg: String,
                            kafkaHosts: List[_],
                          )

  def loadConfig(filePath: String = "application.conf") = {
    ServiceConfig(influxUrl = RtConfig().getStringValue("influx.url"),
      influxAuthToken = RtConfig().getStringValue("influx.authToken"),
      influxOrg = RtConfig().getStringValue("influx.org"),
      kafkaHosts = RtConfig().getArrayValue("kafka.urls").asScala.toList)
  }

  val globalConfig: ZLayer[ZIOAppArgs, Nothing, ServiceConfig] =
    ZLayer.fromZIO(
      ZIO.service[ZIOAppArgs].map {conf =>
        if(conf.getArgs.isEmpty) loadConfig()
        else loadConfig(conf.getArgs.head)
      }
    )

  override val bootstrap: ZLayer[ZIOAppArgs, Nothing, ServiceConfig] = ZLayer.fromZIO(
    ZIO.service[ZIOAppArgs].map { conf =>
      if (conf.getArgs.isEmpty) loadConfig()
      else loadConfig(conf.getArgs.head)
    }
  )

  val myApp = for {
    env <- System.env("app_env")
      .flatMap(x => ZIO.fromOption(x))
      .orElseFail("The environment variable APP_ENV cannot be found.")
    _ <- Console.printLine("APP_ENV => " + env)
  } yield()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    myApp.exitCode

}
