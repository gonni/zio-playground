package c.o.ds.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import c.o.ds.stream.SvcConfigZio.ServiceConfig
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.stream.ZStream
import com.influxdb.client.scala.{InfluxDBClientScala, InfluxDBClientScalaFactory}
import zio._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._

object InfluxClientZio {
  implicit val system: ActorSystem = ActorSystem("influx-client-worker")

  trait InfluxClient {
    def write(fluxDataValue: String)
  }

  case class InfluxClientImpl(client: InfluxDBClientScala) extends InfluxClient {
    override def write(fluxDataValue: String): Unit = {
      println("[INFLUX write]" + fluxDataValue)
      val source = Source.single(fluxDataValue)
      val sink = client.getWriteScalaApi.writeRecord()
      val materialized = source.toMat(sink)(Keep.right)
      //      materialized.run()
      Await.result(materialized.run(), Duration.Inf)
    }
  }

  val layer: ZLayer[ServiceConfig, Throwable, InfluxClient] =
    ZLayer {
      for {
        //          cfgFile <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("application.conf"))
        //          config <- zioSvcConf(cfgFile)
        config <- SvcConfigZio.zioEnvSvcConf()
      } yield (InfluxClientImpl(InfluxDBClientScalaFactory.create(
        config.influxUrl, config.influxAuthToken.toCharArray, config.influxOrg, "clicklog")))
    }

//  object InfluxClientLive {
//    val layer: ZLayer[ServiceConfig, Nothing, InfluxClient] =
//      ZLayer {
//        for {
//          //          cfgFile <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("application.conf"))
//          //          config <- zioSvcConf(cfgFile)
//          config <- SvcConfigZio.zioEnvSvcConf()
//        } yield (InfluxClientImpl(InfluxDBClientScalaFactory.create(
//          config.influxUrl, config.influxAuthToken.toCharArray, config.influxOrg, "logstream")))
//      }
//  }
}
