package c.y.sample.influx

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source}
import com.influxdb.annotations.{Column, Measurement}
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.scala.InfluxDBClientScalaFactory
import com.influxdb.client.write.Point

import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ScalaWriteApi {

  implicit val system: ActorSystem = ActorSystem("examples")

  def main(args: Array[String]): Unit = {
    val secToken = "g4jKhq-LzEQABu7OPmE0Pvr7lVO9UJlb5IfroOZNvMLueLQscGZ7D55x_utgjQTzYICdumY4jhd0zVTuCUcexw=="
    val org = "XSTORE"
    val bucket = "logstream"

    val client = InfluxDBClientScalaFactory.create(
      "http://localhost:8086", secToken.toCharArray, org, bucket)

    //
    // Use InfluxDB Line Protocol to write data
    //
    val record = "mem,host=host2 used_percent=23.43234543"

    val source = Source.single(record)
    val sink = client.getWriteScalaApi.writeRecord()
    val materialized = source.toMat(sink)(Keep.right)
    Await.result(materialized.run(), Duration.Inf)

    println("Data-point Inserted ..")
    //
    // Use a Data Point to write data
    //
    val point = Point
      .measurement("mem")
      .addTag("host", "host3")
      .addField("used_percent", 23.43234541)
      .time(Instant.now(), WritePrecision.NS)

    val sourcePoint = Source.single(point)
    val sinkPoint = client.getWriteScalaApi.writePoint()
    val materializedPoint = sourcePoint.toMat(sinkPoint)(Keep.right)
    Await.result(materializedPoint.run(), Duration.Inf)
    println("Data-point Inserted ..")

    //
    // Use POJO and corresponding class to write data
    //
    val mem = new Mem()
    mem.host = "host3"
    mem.used_percent = 23.43234543
    mem.time = Instant.now

    val sourcePOJO = Source.single(mem)
    val sinkPOJO = client.getWriteScalaApi.writeMeasurement()
    val materializedPOJO = sourcePOJO.toMat(sinkPOJO)(Keep.right)
    Await.result(materializedPOJO.run(), Duration.Inf)
    println("Data-point Inserted ..")

    client.close()
    system.terminate()
  }

  @Measurement(name = "mem")
  class Mem() {
    @Column(tag = true)
    var host: String = _
    @Column
    var used_percent: Double = _
    @Column(timestamp = true)
    var time: Instant = _
  }
}