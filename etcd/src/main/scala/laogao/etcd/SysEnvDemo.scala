package laogao.etcd

import zio._
import zio.console.putStrLn

object SysEnvDemo extends zio.App {

  val program = for {
    _ <- putStrLn("You should be able to get the $PATH via sys.env(\"PATH\"):\n" + sys.env("PATH"))
  } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode

}