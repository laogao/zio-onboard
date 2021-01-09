package laogao.bidi

import zio._
import zio.console._
import zio.stream.ZStream

object ZStreamDemo extends zio.App {

  // ZStream.unwrapManaged {
  //   stream.peel(ZSink.head).map { case (head, rest) =>
  //     ZStream.fromEffect(putStrLn(head)).drain ++
  //     rest.timeout(1.minute)
  //   }
  // }

  def sample(): ZIO[Any, Nothing, Unit] = {
    val s1 = ZStream.repeatEffectWith(ZIO.succeed(SmokeResponse(s"${new java.util.Date()}")), Schedule.spaced(1.seconds))
    val s2 = ZStream.unwrapManaged {
    }
  }

  val program = for {
    _ <- putStrLn("")
    _ <- 
  } yield ()

  final def run(args: List[String]) =
    program.exitCode

}