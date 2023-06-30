package c.y.sample.influx

import akka.actor.ActorSystem
import zio._
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import com.influxdb.annotations.{Column, Measurement}
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.scala.{InfluxDBClientScala, InfluxDBClientScalaFactory}
import com.influxdb.client.write.Point

import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration.Duration


object ZioInfluxMain extends ZIOAppDefault {

  implicit val system: ActorSystem = ActorSystem("examples")

  val secToken = "g4jKhq-LzEQABu7OPmE0Pvr7lVO9UJlb5IfroOZNvMLueLQscGZ7D55x_utgjQTzYICdumY4jhd0zVTuCUcexw=="
  val org = "XSTORE"
  val bucket = "logstream"

  val client = InfluxDBClientScalaFactory.create(
    "http://localhost:8086", secToken.toCharArray, org, bucket)

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

  object InfluxClient {
    def layer: ZLayer[Any, Nothing, InfluxClient] = ZLayer.succeed(InfluxClientImpl(client))
  }

  val myApp: ZIO[InfluxClient, Nothing, Unit] =
    for {
      insert <- ZIO.serviceWith[InfluxClient](_.write("mem,host=host17 used_percent=123.567")).fork
            _ <- Console.printLine("Job Completed ").exitCode
    } yield ()


  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    myApp.debug("debug").provide(InfluxClient.layer)
}
