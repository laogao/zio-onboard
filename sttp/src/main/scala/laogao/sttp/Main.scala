package laogao.sttp

import sttp.client3._
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, sendR}
import zio.console.putStrLn
import zio.{ExitCode, Schedule, ZIO}
import zio.duration._

object Main extends zio.App {

  val req = basicRequest.get(uri"https://github.com/laogao/zio-onboard").response(asStringAlways)

  val program = for {
    response <- sendR(req).either.repeat(
      Schedule.spaced(1.second) *> Schedule.recurs(10) *> Schedule.recurWhile(result => RetryWhen.Default(req, result))
    ).absolve
    _ <- ZIO.foreach_(response.headers)(header => putStrLn(s"${header.name}: ${header.value}"))
    _ <- putStrLn(response.body.substring(0,100))
  } yield response

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    program.provideCustomLayer(AsyncHttpClientZioBackend.layer()).exitCode

}