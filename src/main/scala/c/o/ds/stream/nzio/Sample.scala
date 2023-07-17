package c.o.ds.stream.nzio

import c.o.ds.stream.SvcConfigZio.RtConfig


import scala.jdk.CollectionConverters._

object Sample extends App {
//  val cfg = RtConfig()
//  println("-> " + cfg.getArrayValue("kafka.urls")
//    .asScala.map(a => String.valueOf(a)).toList.mkString(","))

//  val sample = Array[Byte]('a', 'b', '\u0002','c', 'd', 'e', '\u0002', '1', '2')
//  sample.foreach(a => if(a == '\u0002') print("|") else print(a.toChar) )

//  val str = sample.toList.map(a => if(a == '\u0002') "|#@|" else ""+a).mkString
//  println(new String(sample, "UTF-8").split('\u0002').mkString("|"))
//  println("-> " + str.split("\\|\\#\\@\\|").toList)

//  val src = "abc{$[123{$[111{$[{$[{$[1.0"
//  val resrc = src.replaceAll("\\{\\$\\[", "|")
//  println("->" + resrc)

//  val sample = "hostnameCCSs-was10instanceNametstSvr1enprodentityccswap�20230712130205850|2574**745||||02:0***0:00|SCV38|9.0|9999.9.9|2406:da12:5ba:c702:1df7:52a7:d9d6:e10d|MRT00414|1|SC000||1x22300000|||1x22300C01|||0000762806||||||||US2****133|DE20***7600|3.0||"
//
//
//  // ts-0 | UserKey-28 | ProdID-20 | PageCD-14 | P1 | P2 | P3 | PrevCD-17
//  case class ClickLogTsd(ts: String, userKey: String, prodId: String, pageCd: String,
//                         d1: String, d2: String, d3:String, prePageCd: String)
//
//
////  sample.split("\\|").zipWithIndex.foreach(i => println(i._2 + "->" + i._1))
//  val tokenMap = sample.split("\\|").zipWithIndex.map(_.swap).toMap //.map(t => (t._2, t._1)).toMap
//
//  val headerTs = tokenMap(0).substring(tokenMap(0).length - "20230712130205850".length)
//  val pageCd = tokenMap(14)
//  val depthedPageCd = getDepths(pageCd)
//  val res = ClickLogTsd(headerTs, tokenMap(28), tokenMap(20), pageCd,
//    depthedPageCd._1, depthedPageCd._2, depthedPageCd._3, tokenMap(17))
//
//  def getDepths(pageCd: String) = {
//    pageCd.length match {
//      case 10 => (pageCd.substring(0,3), pageCd.substring(3,7), pageCd.substring(7))
//      case _ => ("NA", "NA", "NA")
//    }
//  }
//
//  println("Result -> " + res)


//  val a = "12"
//  println(a.equals("12"))

  println("ts -> " + System.currentTimeMillis())

  //20230712130205850 --> 2023-07-12 13:02:05.850
//  import java.time.format.DateTimeFormatter
  import java.text.SimpleDateFormat
  val fmt = new SimpleDateFormat("yyyyMMddHHmmssSSS")
  val ts = fmt.parse("20230712130205850")
  println("ts -> " + ts.getTime)

}
