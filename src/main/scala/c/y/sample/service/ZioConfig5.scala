package c.y.sample.service

import c.y.sample.service.SimpleApp.BOOSTRAP_SERVERS
import com.typesafe.config.ConfigFactory
import zio._
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde

import scala.jdk.CollectionConverters._

object ZioConfig5 extends ZIOAppDefault {
//  private val BOOSTRAP_SERVERS = List("localhost:29092")

  case class ServiceConfig(
                            influxUrl: String,
                            influxAuthToken: String,
                            influxOrg: String,
                            kafkaHosts: List[String],
                          )

  val myConfig: ZLayer[Any, Config.Error, ServiceConfig] =
    ZLayer.succeed(
      ServiceConfig(influxUrl = RtConfig().getStringValue("influx.url"),
        influxAuthToken = RtConfig().getStringValue("influx.authToken"),
        influxOrg = RtConfig().getStringValue("influx.org"),
        kafkaHosts = RtConfig().getArrayValue("kafka.urls").asScala.map(a => String.valueOf(a)).toList
      )
    )

  def zioConfig(confFile: String = "application.conf") = ZIO.succeed {
    val rtConf = RtConfig(confFile)

    ServiceConfig(
      influxUrl = rtConf.getStringValue("influx.url"),
      influxAuthToken = rtConf.getStringValue("influx.authToken"),
      influxOrg = rtConf.getStringValue("influx.org"),
      kafkaHosts = rtConf.getArrayValue("kafka.urls").asScala.map(a => String.valueOf(a)).toList)
  }

  private def consumeAndPrintEvents(
                                      kafkaServers: List[String],
                                      groupId: String,
                                      topic: String,
                                      topics: String*
                                   ): RIO[Any, Unit] =
    Consumer.consumeWith(
      settings = ConsumerSettings(kafkaServers)
        .withGroupId(groupId),
      subscription = Subscription.topics(topic, topics: _*),
      keyDeserializer = Serde.long,
      valueDeserializer = Serde.string
    )(record => Console.printLine((record.key(), record.value())).orDie)


  // 1. Argument by console
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      conf <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("application.conf"))
//      port <- ZIO.config(Config.int("port"))
      _ <- Console.printLine(s"Conf-file: $conf")
      sysConf <- zioConfig(conf)
      _ <- Console.printLine("System Conf -> " + sysConf)
      f <- consumeAndPrintEvents(sysConf.kafkaHosts, "yg", "my-topic").fork
      _ <- Console.printLine("...")
      _ <- f.join
    } yield ()
  }

}
