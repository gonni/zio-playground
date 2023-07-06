package c.y.sample.service

import akka.actor.ActorSystem
import zio._
import org.apache.kafka.clients.producer.ProducerRecord
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream

object LogStreaming2Main extends ZIOAppDefault {
  implicit val system: ActorSystem = ActorSystem("influx-client-worker")

  private val BOOSTRAP_SERVERS = List("localhost:29092")
  private val KAFKA_TOPIC = "my-topic"

  private val consumer: ZLayer[Any, Throwable, Consumer] =
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(BOOSTRAP_SERVERS)
          .withGroupId("streaming-kafka-app")
      )
    )

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val c: ZStream[Consumer, Throwable, Nothing] =
      Consumer
        .plainStream(Subscription.topics(KAFKA_TOPIC), Serde.int, Serde.string)
        .tap(e => Console.printLine(e.value))
        .map(_.offset)
        .aggregateAsync(Consumer.offsetBatches)
        .mapZIO(_.commit)
        .drain




    c.runDrain.provide(consumer)
  }

//  def sendData
}
