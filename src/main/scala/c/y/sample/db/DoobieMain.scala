package c.y.sample.db

import cats.effect._
import doobie._
import doobie.implicits._



object DoobieMain extends App {

  println("Active Doobie App ..")
  import cats.effect.unsafe.implicits.global

  val tx: Transactor[IO] = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://horus.cyd0dnq4tebb.ap-northeast-2.rds.amazonaws.com:3306/horus?useUnicode=true&characterEncoding=utf8&useSSL=false",
    "admin",
    "18651865"
  )

  sql"select ANCHOR_TEXT from crawl_unit1 order by CRAWL_NO desc"
    .query[Option[String]]
    .to[List]
    .transact(tx)
    .unsafeRunSync()
    .take(10)
    .foreach(println)

}
