package c.y.sample.service

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.stream.ZStream
import c.y.sample.service.ZioConfig.ServiceConfig
import com.influxdb.client.scala.{InfluxDBClientScala, InfluxDBClientScalaFactory}
import zio._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object LogStreamingMain extends ZIOAppDefault {
  implicit val system: ActorSystem = ActorSystem("influx-client-worker")

//  val secToken = "apKD5bj-T88DrXIOzYzJJIroufE4LMczCHKzasxRyCo8npL1VSjBlzY1QPKwVRJ5WgX_Uc_B-Swc9kKt7qbczg=="
//  val org = "KYG"
//  val bucket = "logstream"

//  val client = InfluxDBClientScalaFactory.create(
//    "http://localhost:8086", secToken.toCharArray, org, bucket)

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
    def layer: ZLayer[ServiceConfig, Nothing, InfluxClient] = //ZLayer.succeed(InfluxClientImpl(client))
      ZLayer{
        for{
          config <- ZIO.service[ServiceConfig]
        } yield( InfluxClientImpl(InfluxDBClientScalaFactory.create(
          config.influxUrl, config.influxAuthToken.toCharArray, config.influxOrg, "logstream")))
      }
  }



//  def createConsumer (host: List[String]) = {
//    Consumer.make(
//      ConsumerSettings(host).withGroupId("group")
//    )
//  }
  // --- Kafka Consumer ---
  def consumerLayer: ZLayer[Any, Throwable, Consumer] = {
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("localhost:29092")).withGroupId("group")
        //        ConsumerSettings().withGroupId("group")
      )
    )
  }

  val myConsumer: ZStream[Consumer with InfluxClient, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("my-topic"), Serde.long, Serde.string)
      .tap(r => syncLogData(r.value))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  private def syncLogData(logLine: String): ZIO[InfluxClient, Throwable, Unit] =
    for {
      _ <- Console.printLine(s"message from Kafka: [$logLine]")
      kd <- ZIO.succeed(s"mem,host=$logLine used_percent=" + (100 * Math.random()))
      _ <- ZIO.serviceWith[InfluxClient](_.write(kd)).fork
      _ <- Console.printLine(s"Data Inserted to Kafka: $kd")
    } yield ()

  private val BOOSTRAP_SERVERS = List("localhost:29092")

//  private def consumeAndPrintEvents(
//                                     groupId: String,
//                                     topic: String,
//                                     topics: String*
//                                   ): RIO[Any, Unit] =
//    Consumer.consumeWith(
//      settings = ConsumerSettings(BOOSTRAP_SERVERS)
//        .withGroupId(groupId),
//      subscription = Subscription.topics(topic, topics: _*),
//      keyDeserializer = Serde.long,
//      valueDeserializer = Serde.string
//    ) { record =>
//      //Console.printLine((record.key(), record.value())).orDie
//      syncLogData(record.value()).orDie
//    }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
//  {
////    val a = for {
////      f <- consumeAndPrintEvents("mygroup", "my-topic").fork
////      _ <- Console.printLine("Kakfa Job Processiong ..")
////      _ <- f.join
////      _ <- Console.printLine("App Completed !!")
////    } yield()
//    val a = consumeAndPrintEvents("mygroup", "my-topic")
//    a.provide(ServiceConfig.layer2, consumerLayer, InfluxClient.layer)
//  }
      myConsumer.runDrain.provide(
      ServiceConfig.layer2, consumerLayer, InfluxClient.layer)
}
