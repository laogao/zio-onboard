package laogao.bidi

import zio._
import zio.console._

object UUIDGenerator extends zio.App {

  def myAppLogic = for {
    _ <- putStrLn(java.util.UUID.randomUUID().toString).repeat(Schedule.recurs(2749))
  } yield ()

  final def run(args: List[String]) =
    myAppLogic.exitCode

}