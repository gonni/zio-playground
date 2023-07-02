package c.y.sample.zio

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import c.y.sample.kafka.ClickLogStream.syncInfluxZio
import com.influxdb.client.scala.{InfluxDBClientScala, InfluxDBClientScalaFactory}
import zio._
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.stream.ZStream

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Kafka2Influx extends ZIOAppDefault{

  implicit val system: ActorSystem = ActorSystem("examples")

  //  val secToken = "g4jKhq-LzEQABu7OPmE0Pvr7lVO9UJlb5IfroOZNvMLueLQscGZ7D55x_utgjQTzYICdumY4jhd0zVTuCUcexw=="
  //  val org = "XSTORE"
  val secToken = "apKD5bj-T88DrXIOzYzJJIroufE4LMczCHKzasxRyCo8npL1VSjBlzY1QPKwVRJ5WgX_Uc_B-Swc9kKt7qbczg=="
  val org = "KYG"
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

  // ----- Kafka -----
  val myConsumer: ZStream[Consumer with InfluxClient, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("my-topic"), Serde.long, Serde.string)
      .tap(r => syncInfluxZio1(r.value))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  private def syncInfluxZio1(logLine: String): ZIO[InfluxClient, Throwable, Unit] =
    for {
      _ <- Console.printLine(s"message from Kafka: [$logLine]")
      kd <- ZIO.succeed(s"mem,host=$logLine used_percent=" + (100 * Math.random()))
      _ <- ZIO.serviceWith[InfluxClient](_.write(kd)).fork
      _ <- Console.printLine(s"Data Inserted to Kafka: $kd")
    } yield()


  def consumerLayer: ZLayer[Any, Throwable, Consumer] =
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("localhost:29092")).withGroupId("group")
      )
    )

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    myConsumer.runDrain.provide(
      consumerLayer, InfluxClient.layer)
}
