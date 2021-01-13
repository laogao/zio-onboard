package laogao.etcd

import com.typesafe.config.ConfigFactory
import zio._
import zio.console.putStrLn

object TypesafeConfigDemo extends zio.App {

  val config = ConfigFactory.load()

  val program = for {
    _ <- putStrLn("First let's try Typesafe config to read environment variables.")
    _ <- putStrLn("Note that you have to place all config keys in application.conf, as such:")
    _ <- putStrLn("sys {\n path = ${?PATH}\n}")
    _ <- putStrLn("Then you can val config = ConfigFactory.load() and config.getString(\"sys.path\")")
    _ <- putStrLn("You should now be able to get the $PATH:\n" + config.getString("sys.path"))
  } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode

}