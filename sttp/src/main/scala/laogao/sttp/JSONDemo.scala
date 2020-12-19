package laogao.sttp

import sttp.client3._
import sttp.client3.circe._
import sttp.client3.asynchttpclient.zio._
import io.circe.generic.auto._
import zio._
import zio.console.Console

object JSONDemo extends zio.App {

  case class HttpBinResponse(origin: String, headers: Map[String, String])

  val request = basicRequest
    .get(uri"https://httpbin.org/get")
    .response(asJson[HttpBinResponse])

  val sampleResponse =
    """
      |{
      |  "args": {},
      |  "headers": {
      |    "Accept": "*/*",
      |    "Host": "httpbin.org",
      |    "User-Agent": "curl/7.64.1",
      |    "X-Amzn-Trace-Id": "Root=1-5fddcc2f-7d87d2546c36786b066d0099"
      |  },
      |  "origin": "50.7.11.22",
      |  "url": "https://httpbin.org/get"
      |}
      |""".stripMargin

  val program: ZIO[Console with SttpClient, Throwable, Unit] = for {
    response <- send(request)
    _ <- console.putStrLn(s"Got response code: ${response.code}")
    _ <- console.putStrLn(response.body.toString)
  } yield ()

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    program.provideCustomLayer(AsyncHttpClientZioBackend.layer()).exitCode

}