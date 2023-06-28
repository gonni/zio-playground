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

  def influxCliet: [Any, Throwable, InfluxDBClientScala] = ZLayer.fromFunction()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = ???
}
