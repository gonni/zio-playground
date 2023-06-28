package c.y.sample.zlayer
/*

import zio._
import zio.console._

object ZLayersPlaygroud extends zio.App {

  val meaningOfLife = ZIO.succeed(42)
  val aFailure = ZIO.fail("Something went wrong")

  val greeting = for {
    _ <- putStr("Hi, what's your name?")
    name <- getStrLn
    _ <- putStr(s"Your name is $name")
  } yield ()


  // --
  case class User(name: String, email: String)

  object UserEmailer {
    type UserEmailerEnv = Has[UserEmailer.Service]
    // service def
    trait Service {
      def notify(user: User, message: String): Task[Unit] // ZIO[Any, Throwable, Unit]
    }

    // service impl
    val live: ZLayer[Any, Nothing, Has[UserEmailer.Service]] = ZLayer.succeed(new Service {
      override def notify(user: User, message: String): Task[Unit] =
        Task {
          println(s"[Z-UserEmailer] Sending $message to ${user.email}")
        }
    })

    // front-facing api
    def notify(user: User, message: String): ZIO[Has[UserEmailer.Service], Throwable, Unit] =
      ZIO.accessM(hasService => hasService.get.notify(user, message))
  }

  // service logic2
  object UserDb {
    type UserDbEnv = Has[UserDb.Service]
    trait Service {
      def insert(user: User): Task[Unit]
    }

    val live = ZLayer.succeed(new Service {
      override def insert(user: User): Task[Unit] = Task {
        println(s"[DB] insert into public.user values (${user.email})")
      }
    })

    def insert(user: User): ZIO[UserDbEnv, Throwable, Unit] = ZIO.accessM(_.get.insert(user))
  }

  // HORIZONTAL Composition

  import UserDb._
  import UserEmailer._
  val userBackendLayer: ZLayer[Any, Nothing, UserDbEnv with UserEmailerEnv] = UserDb.live ++ UserEmailer.live

  // VERTICAL composition
  object UserSubscription {
    type UserSubscriptionEnv = Has[UserSubscription.Service]

    class Service(notifier: UserEmailer.Service, userDb: UserDb.Service) {
      def subscribe(user: User): Task[User] = for {
        _ <- userDb.insert(user)
        _ <- notifier.notify(user, s"Welcome to hell " + user.name)
      } yield user
    }

    val live: ZLayer[UserEmailerEnv with UserDbEnv, Nothing, UserSubscriptionEnv] =
      ZLayer.fromServices[UserEmailer.Service, UserDb.Service, UserSubscription.Service] { (userEmailer, userDb) =>
        new Service(userEmailer, userDb)
    }

    def subscribe(user: User): ZIO[UserSubscriptionEnv, Throwable, User] = ZIO.accessM(_.get.subscribe(user))
  }

  import UserSubscription._
  val userUserSubscriptinoLayer: ZLayer[Any, Nothing, UserSubscriptionEnv] = userBackendLayer >>> UserSubscription.live

  val user1 = User("Dana", "dna@na.com")
  val msg = "This is a message on ZIO"

  def notifyDaniel() = UserEmailer.notify(user1, msg)
    //      .provideLayer(UserEmailer.live)
    .provideLayer(userBackendLayer)
    .exitCode

  def run(args: List[String]) =
    UserSubscription.subscribe(user1)
      .provideLayer(userUserSubscriptinoLayer).exitCode

}*/
