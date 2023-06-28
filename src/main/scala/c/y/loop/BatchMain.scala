package c.y.loop

//import zio.Schedule.recurs
//import zio._
//import zio.clock._
//import zio.duration.durationInt
//
//object BatchMain extends App {
//  println("Active System ..")
//
//  val effect: Task[Unit] = ZIO.effect(println("Hello"))
//  val repeatingSchedule: ZIO[Any with Clock, Throwable, Long] =
//    effect.repeat(Schedule.spaced(1000.milliseconds) >>> recurs(10))
//
//  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = repeatingSchedule.exitCode
//}
