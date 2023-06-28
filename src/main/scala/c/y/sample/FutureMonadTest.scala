package c.y.sample

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

object FutureMonadTest {
  def main(args: Array[String]): Unit = {
    val a = Future[Int] {
      Thread.sleep(3000)
      println("Future A completed ..")
      2
    }

    val b = Future[Int] {
      Thread.sleep(1000)
      println("Future B completed ..")
      3
    }

    val b2 = (base: Int) => Future[Int] {
      Thread.sleep(1000)
      val res = base * 2
      println("Future B2 completed => " + res)
      res
    }

    val c = (a: Int, b: Int) => Future {
      println(s"a = ${a}, b = ${b}")
      a * b
    }


    val result = for {
      va <- a
      vb <- b
      vb2 <- b2(vb)
      vc <- c(va, vb2)
    } yield vc

    val res = Await.result(result, 10.second).toInt
    println("Final Result =>" + res)
  }
}
