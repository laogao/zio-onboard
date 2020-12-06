package laogao.layers

import zio._
import zio.console.{Console, putStrLn}

object Main extends zio.App {
  case class User(name: String)
  type Logging = Has[Logging.Service]
  object Logging {
    trait Service {
      def log(msg: String): UIO[Unit]
    }
    def log(msg: String): ZIO[Logging, Nothing, Unit] = ZIO.accessM[Logging](_.get.log(msg))
    val viaConsole: ZLayer[Console, Nothing, Logging] = ZLayer.fromService(console =>
      new Service {
        override def log(msg: String): UIO[Unit] = console.putStrLn(msg)
      }
    )
  }
  type Greeter = Has[Greeter.Service]
  object Greeter {
    // API
    trait Service {
      def greet(user: User): UIO[Unit]
    }
    // Default Implementation via Console
    val viaConsole: ZLayer[Console, Nothing, Greeter] = ZLayer.fromService(console => // access RIn (Console)
      new Service {
        override def greet(user: User): UIO[Unit] = for {
          res <- console.putStrLn(s"Hello, ${user.name}!")
        } yield res
      }
    )
    def greet(user: User): ZIO[Greeter, Nothing, Unit] = ZIO.accessM[Greeter](_.get.greet(user))
    def greetWithLogging(user: User): ZIO[Greeter with Logging, Nothing, Unit] =
        ZIO.accessM[Logging](_.get.log(s"rec'd user info $user")) *> ZIO.accessM[Greeter](_.get.greet(user))
  }
  val program = Greeter.greetWithLogging(User("Jon"))
  val dependencies = Console.live >>> Greeter.viaConsole ++ Logging.viaConsole
  val dependenciesCompact = Greeter.viaConsole ++ Logging.viaConsole // since we're using exitCode, Console is optional
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.provideLayer(dependenciesCompact).exitCode

}
