package c.y.sample.service

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.stream.ZStream
import com.influxdb.client.scala.{InfluxDBClientScala, InfluxDBClientScalaFactory}
import zio._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._

object LogStream3Main extends ZIOAppDefault {
  implicit val system: ActorSystem = ActorSystem("influx-client-worker")

  // ConsumerSettings(List("localhost:29092")).withGroupId("group")
  case class ServiceConfig(
                            influxUrl: String,
                            influxAuthToken: String,
                            influxOrg: String,
                            kafkaHosts: List[String],
                          )

  def loadConfig(filePath: String = "application.conf") = {
    ServiceConfig(influxUrl = RtConfig().getStringValue("influx.url"),
      influxAuthToken = RtConfig().getStringValue("influx.authToken"),
      influxOrg = RtConfig().getStringValue("influx.org"),
      kafkaHosts = RtConfig().getArrayValue("kafka.urls").asScala.map(a => String.valueOf(a)).toList)
  }

  def zioSvcConf(file: String = "application.conf") = ZIO.succeed(loadConfig(file))

  def zioEnvSvcConf(file: String = "application.conf") = for {
    f <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("error.conf"))
    _ <- Console.printLine("Init CFG file :" + f).orElse(ZIO.succeed())
    cfgFile <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("application.conf"))
  } yield loadConfig(cfgFile)

  object ConfLive {
    val layer: TaskLayer[ServiceConfig] = ZLayer.fromZIO(zioSvcConf())
  }

  trait InfluxClient {
    def write(fluxDataValue: String)
  }

  case class InfluxClientImpl(client: InfluxDBClientScala) extends InfluxClient {
    override def write(fluxDataValue: String): Unit = {
      val source = Source.single(fluxDataValue)
      val sink = client.getWriteScalaApi.writeRecord()
      val materialized = source.toMat(sink)(Keep.right)
      //      materialized.run()
      Await.result(materialized.run(), Duration.Inf)
    }
  }

  object InfluxClientLive {
    val layer : ZLayer[ServiceConfig, Nothing, InfluxClient] =
      ZLayer {
        for {
//          cfgFile <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("application.conf"))
//          config <- zioSvcConf(cfgFile)
          config <- zioEnvSvcConf()
        } yield (InfluxClientImpl(InfluxDBClientScalaFactory.create(
          config.influxUrl, config.influxAuthToken.toCharArray, config.influxOrg, "logstream")))
      }
  }


  def zioMakeConsumer(kafkaUrls: List[String]): ZIO[Scope, Throwable, Consumer] =
    Consumer.make(ConsumerSettings(kafkaUrls).withGroupId("group"))

  def scoped: ZIO[Scope, Throwable, Consumer] =
    for{
      _ <- Console.printLine("Consumer Init ...")
//      cfgFile <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("application.conf"))
//      conf <- zioSvcConf(cfgFile)
      conf <- zioEnvSvcConf()
      consumer <- zioMakeConsumer(conf.kafkaHosts)
    } yield consumer

  object ConsumerLive {
    val layer: ZLayer[Any, Throwable, Consumer] = ZLayer.scoped(scoped)
  }

  val myConsumer: ZStream[Consumer with InfluxClient, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("my-topic"), Serde.long, Serde.string)
//      .tap(r => Console.printLine("received -> " + r.value))
      .tap(r => syncLogData(r.value))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  private def syncLogData(logLine: String): ZIO[InfluxClient, Throwable, Unit] =
    for {
      _ <- Console.printLine(s"message from Kafka: [$logLine]")
      kd <- ZIO.succeed(s"mem,host=$logLine used_percent=" + (100 * Math.random()))
      _ <- ZIO.serviceWith[InfluxClient](_.write(kd)) //.fork
      _ <- Console.printLine(s"Data Inserted to Kafka: $kd")
    } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    for {
      conf <- ZIO.config(Config.string("conf"))
      _ <- Console.printLine("Detected Config File by Argument :" + conf)
    } yield()

    myConsumer
      .runDrain
      .provide(
        ConfLive.layer,
        ConsumerLive.layer,
        InfluxClientLive.layer
      )
  }
}
