package c.y.sample.kafka

import org.apache.kafka.clients.producer.RecordMetadata
import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._

/**
 * influxdb
 * x6jJ6zDVRSnx9S-LTEeeCizSfgP9mrvINnBfFjVPBqommLkRiTPd_QmDGTLhrfE3N-knBx4H3QSDNplzi2MhSQ==
 * XSTORE
 */
object SimpleApp extends ZIOAppDefault {
  private val BOOSTRAP_SERVERS = List("localhost:29092")
  private val KAFKA_TOPIC = "hello"

  private def produce(
                       topic: String,
                       key: Long,
                       value: String
                     ): RIO[Any with Producer, RecordMetadata] =
    Producer.produce[Any, Long, String](
      topic = topic,
      key = key,
      value = value,
      keySerializer = Serde.long,
      valueSerializer = Serde.string
    )

  private def consumeAndPrintEvents(
                                     groupId: String,
                                     topic: String,
                                     topics: String*
                                   ): RIO[Any, Unit] =
    Consumer.consumeWith(
      settings = ConsumerSettings(BOOSTRAP_SERVERS)
        .withGroupId(groupId),
      subscription = Subscription.topics(topic, topics: _*),
      keyDeserializer = Serde.long,
      valueDeserializer = Serde.string
    )(record => Console.printLine((record.key(), record.value())).orDie)

  private val producer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(
      Producer.make(
        ProducerSettings(BOOSTRAP_SERVERS)
      )
    )

  def run =
    for {
      f <- consumeAndPrintEvents("my-consumer-group", KAFKA_TOPIC).fork
      _ <-
        Clock.currentDateTime
          .flatMap { time =>
            produce(KAFKA_TOPIC, time.getHour, s"$time -- Hello, World!")
          }
          .schedule(Schedule.spaced(1.second))
          .provide(producer)
      _ <- f.join
    } yield ()

}