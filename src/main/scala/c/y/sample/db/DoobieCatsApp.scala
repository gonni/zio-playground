package c.y.sample.db

import cats.effect._
import doobie._
import doobie.implicits._

object DoobieCatsApp extends IOApp {

  val tx: Transactor[IO] = Transactor.fromDriverManager[IO](
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://horus.cyd0dnq4tebb.ap-northeast-2.rds.amazonaws.com:3306/horus?useUnicode=true&characterEncoding=utf8&useSSL=false",
    "admin",
    "18651865"
  )

  def findAnchorText(): IO[List[Option[String]]] = {
    sql"select ANCHOR_TEXT from crawl_unit1 order by CRAWL_NO desc limit 10"
      .query[Option[String]]
      .to[List]
      .transact(tx)
  }


  override def run(args: List[String]): IO[ExitCode] =
    findAnchorText().map(println).as(ExitCode.Success)
}
