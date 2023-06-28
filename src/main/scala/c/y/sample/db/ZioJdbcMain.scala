package c.y.sample.db

//import zio._
//import zio.jdbc._
//import zio.schema.Schema
//
//object ZioJdbcMain extends ZIOAppDefault {
//
//  val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
//    ZLayer.succeed(ZConnectionPoolConfig.default)
//
//  val properties = Map(
//    "user" -> "admin",
//    "password" -> "18651865"
//  )
//
//  val connectionPool: ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool] =
//    ZConnectionPool.mysql("horus.cyd0dnq4tebb.ap-northeast-2.rds.amazonaws.com",
//      3306, "horus", properties)
//
////  val live: ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool] = createZIOPoolConfig >>> connectionPool
//
//  val res1: ZIO[ZConnectionPool, Throwable, Chunk[String]] =
//    transaction {
//      selectAll {
//        sql"select ANCHOR_TEXT from crawl_unit1 order by CRAWL_NO desc limit 100".as[String]
//      }
//    }
//
//  val program: ZIO[ZConnectionPool, Throwable, Chunk[String]] = for {
//    res <- res1
//  }  yield  res
//
//  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = for {
//    res <- program.provideLayer(createZIOPoolConfig >>> connectionPool)
//    _ <- Console.printLine(s"->$res")
//  } yield ()
//}
