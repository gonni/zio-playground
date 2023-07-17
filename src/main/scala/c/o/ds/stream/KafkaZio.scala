package c.o.ds.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import c.o.ds.stream.InfluxClientZio.InfluxClient
import c.o.ds.stream.SvcConfigZio.{ServiceConfig, zioEnvSvcConf}
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.stream.ZStream
import com.influxdb.client.scala.{InfluxDBClientScala, InfluxDBClientScalaFactory}
import org.apache.kafka.clients.consumer.ConsumerConfig
import zio._

import java.util.Properties
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._

object KafkaZio {

  import java.text.SimpleDateFormat
  val fmt = new SimpleDateFormat("yyyyMMddHHmmssSSS")

  def zioMakeConsumer(kafkaUrls: List[String]): ZIO[Scope, Throwable, Consumer] = {
    Consumer.make(ConsumerSettings(kafkaUrls).withGroupId("recc_streaming_grp1")
      .withProperties(
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"),
        (ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")))
  }

  def scoped: ZIO[Scope, Throwable, Consumer] =
    for {
      _ <- Console.printLine("Consumer Init ...")
      //      cfgFile <- ZIO.config(Config.string("conf")).orElse(ZIO.succeed("application.conf"))
      //      conf <- zioSvcConf(cfgFile)
      conf <- SvcConfigZio.zioEnvSvcConf()
      consumer <- zioMakeConsumer(conf.kafkaHosts)
    } yield consumer

  val layer: ZLayer[Any, Throwable, Consumer] = ZLayer.scoped(scoped)

  val myConsumer2: ZStream[InfluxClient with Consumer, Throwable, Nothing] = {
    for {
      conf <- ZStream.fromZIO(SvcConfigZio.zioEnvSvcConf())
      consumer <- Consumer
        .plainStream(Subscription.topics(conf.topic), Serde.long, Serde.byteArray)
//        .tap(r => Console.printLine("received -> " + r.value.toString))
        .tap(r => sycLogData(r.value))
        .map(_.offset)
        .aggregateAsync(Consumer.offsetBatches)
        .mapZIO(_.commit)
        .drain
    } yield consumer
  }

//  private def syncLogData(logLine: String): ZIO[InfluxClient, Throwable, Unit] =
//    for {
//      _ <- ZIO.logInfo(s"msg from KAFKA: [$logLine]")
//      _ <- Console.printLine(s"message from Kafka: [$logLine]")
//      kd <- ZIO.succeed(s"mem,host=$logLine used_percent=" + (100 * Math.random()))
//      _ <- ZIO.serviceWith[InfluxClient](_.write(kd)) //.fork
//      _ <- Console.printLine(s"Data Inserted to Kafka: $kd")
//    } yield ()

  private def sycLogData(data: Array[Byte]) : ZIO[InfluxClient, Throwable, Unit] = {
//    val logLine = new String(data, "UTF-8").split('\u0002').mkString("|")
    val logLine = new String(data, "UTF-8").replaceAll("\\{\\$\\[", "|")
//    println("[TSL]=>" + getTsLog(logLine))
//    val tokens = logLine.split("\\|")

    for {
      tsLog <- ZIO.succeed(getTsLog((logLine)))
//      _ <- Console.printLine(s"Parsed TsLog -> $tsLog")
      influx <- ZIO.service[InfluxClient]
      _ <- ZIO.when(tsLog.p3 == "CPC" || tsLog.p3 == "CPF" || tsLog.p3 == "DRT" || tsLog.p3 == "DRP" || tsLog.p3 == "DRV") {
        val wQry = s"clogTopN,mbr_no=${tsLog.userKey}" +
          s",prod_id=${tsLog.prodId},page_cd=${tsLog.pageCd},act_cd=${tsLog.d3} hit=1 " + convertDatetimeStamp(tsLog.ts)
//        ZIO.serviceWith[InfluxClient](_.write(wQry))
        influx.write(wQry.trim)  // --
        Console.printLine(s"[FIN] Qry for Influx -> $wQry")
      }.fork
    } yield ()
  }

  def convertDatetimeStamp(str: String): String =
    if(str.length == "20230712130205850".length)
      fmt.parse(str).getTime + "000000"
    else
      ""

  // parse event data
  // ts-0 | UserKey-28 | ProdID-20 | PageCD-14 | P1 | P2 | P3 | PrevCD-17
  case class ClickLogTsd(ts: String, userKey: String, prodId: String, pageCd: String,
                         d1: String, d2: String, d3: String, prePageCd: String,
                         p1: String, p2: String, p3: String)

  def getTsLog(logdata: String): ClickLogTsd = {
    val tokenMap = logdata.split("\\|").zipWithIndex.map(_.swap).toMap //.map(t => (t._2, t._1)).toMap

    def getDepths(pageCd: String) = {
      pageCd.length match {
        case 10 => (pageCd.substring(0, 3), pageCd.substring(3, 7), pageCd.substring(7))
        case _ => ("NA", "NA", "NA")
      }
    }

    val headerTs = tokenMap(0).substring(tokenMap(0).length - "20230712130205850".length)
    val pageCd = tokenMap(14)
    val depthedPageCd = getDepths(pageCd)
    val dPrePageCd = getDepths(tokenMap(17))

    ClickLogTsd(headerTs, tokenMap(28), tokenMap(20), pageCd,
      depthedPageCd._1, depthedPageCd._2, depthedPageCd._3, tokenMap(17),
      dPrePageCd._1, dPrePageCd._2, dPrePageCd._3)
  }

}
