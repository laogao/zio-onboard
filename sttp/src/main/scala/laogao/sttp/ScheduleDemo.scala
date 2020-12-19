package laogao.sttp

import sttp.client3._
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, sendR}
import zio.console.putStrLn
import zio.duration._
import zio.{ExitCode, IO, Schedule, ZIO}

object ScheduleDemo extends zio.App {

  val tick = for {
    ms <- ZIO.effectTotal(System.currentTimeMillis())
    _ <- putStrLn(s"${new java.util.Date(ms)} [$ms]")
    res <- if (ms % 10 < 1) ZIO.succeed("we have a winner!") else ZIO.fail("no luck!")
    _ <- putStrLn(res)
  } yield res

  val program = for {
    _ <- tick.either.repeat(
      Schedule.spaced(1.second) *> Schedule.recurs(20) *> Schedule.recurUntil(result => result match {
        case Left(_) => false
        case Right(_) => true
      })
    ).absolve
  } yield ()

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    program.exitCode

}