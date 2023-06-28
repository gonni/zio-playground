package c.y.sample.kafka

import org.apache.kafka.clients.producer.RecordMetadata
import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream

object ClickLogStream extends ZIOAppDefault {

//  val consumer: ZStream[Consumer, Throwable, Nothing] =
//    Consumer
//      .plainStream(Subscription.topics("my-topic"), Serde.long, Serde.string)
//      .tap(r => Console.printLine(r.value))
//      .map(_.offset)
//      .aggregateAsync(Consumer.offsetBatches)
//      .mapZIO(_.commit)
//      .drain

  val myConsumer: ZStream[Consumer, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("my-topic"), Serde.long, Serde.string)
      .tap(r => syncInfluxZio(r.value))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  val syncInflux = (logline: String) => {
    println("Message from Kafka: " + logline)


  }

  def syncInfluxZio(logLine: String) : ZIO[Any, Throwable, Unit] =
    ZIO.succeed(syncInflux(logLine))

  def consumerLayer =
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("localhost:29092")).withGroupId("group")
      )
    )

  override def run =
    myConsumer.runDrain.provide(consumerLayer)

}
