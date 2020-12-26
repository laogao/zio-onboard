package laogao.bidi

import laogao.bidi.bidi.ZioBidi.PipeClient
import laogao.bidi.bidi.SmokeRequest
import io.grpc.ManagedChannelBuilder
import zio._
import zio.console._
import zio.stream.ZStream
import scalapb.zio_grpc.ZManagedChannel

object Main extends zio.App {

  val channel: ManagedChannelBuilder[_] = ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
  val clientManaged = PipeClient.managed(ZManagedChannel(channel))

  def myAppLogic = for {
    _ <- putStrLn("Expect the server to pass down a new message every second. Type '\\q' in between to exit...")
    _ <- clientManaged.use(
      _.smoke(ZStream.repeatEffect(getStrLn.flatMap(line =>
        if ("\\q".equals(line)) ZIO.fail(io.grpc.Status.UNKNOWN) else ZIO.succeed(SmokeRequest(line))
      ).catchAll(_ => ZIO.fail(io.grpc.Status.UNKNOWN)))).foreach(resp => putStrLn(s"GOT: ${resp.message}"))
    )
  } yield ()

  final def run(args: List[String]) =
    myAppLogic.provideCustomLayer(PipeServer.serverLive).exitCode

}