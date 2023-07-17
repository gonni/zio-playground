package c.o.ds.stream.nzio

import c.o.ds.stream.SvcConfigZio.RtConfig

import java.util.{Collections, Properties}
import java.util.regex.Pattern
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.jdk.CollectionConverters._

object ConsumerMain {
  def main(args: Array[String]): Unit = {
    val cfg = RtConfig()

    val props: Properties = new Properties()
    props.put("group.id", "log_streaming_test")

    val bootstrapServers: String = cfg.getArrayValue("kafka.urls").asScala.map(a => String.valueOf(a)).toList.mkString(",")
    println("bootstrap servers-> " + bootstrapServers)

    props.put("bootstrap.servers", bootstrapServers)
    //    cfg.getArrayValue("kafka.urls")
    //      .asScala.map(a => String.valueOf(a)).toList.mkString(","))
    props.put("key.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("enable.auto.commit", "false")
    props.put("auto.commit.interval.ms", "1000")
    //  props.put("auto.offset.reset", "latest")

    val consumer = new KafkaConsumer(props)
    val topics = List(cfg.getStringValue("kafka.topic"))

    try {
      consumer.subscribe(topics.asJava)
      while (true) {
        val records = consumer.poll(10)
        records.forEach(println)
        //      records.asScala.map(_.value()).map(println)
        records.asScala
      }
    }
    catch {
      case e: Exception => e.printStackTrace()
    }
    finally {
      consumer.close()
    }
  }
}
