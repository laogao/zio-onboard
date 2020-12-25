package laogao.bidi

import laogao.bidi.bidi.ZioBidi.ZPipe
import laogao.bidi.bidi.ZioBidi.PipeClient
import laogao.bidi.bidi.SmokeRequest
import laogao.bidi.bidi.SmokeResponse
import io.grpc.{ManagedChannelBuilder, Metadata}
import zio.console._
import scalapb.zio_grpc.{SafeMetadata, ZManagedChannel}

object Main extends zio.App {

  val channel: ManagedChannelBuilder[_] = ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
  val clientManaged = PipeClient.managed(ZManagedChannel(channel))

  def myAppLogic = for {
    _ <- putStrLn("Pipe server should be running now. You can verify via grpcurl.")
    // TODO make call to server
    _ <- putStrLn("Press any key to exit...")
    _ <- getStrLn
  } yield ()

  final def run(args: List[String]) =
    myAppLogic.provideCustomLayer(PipeServer.serverLive).exitCode

}