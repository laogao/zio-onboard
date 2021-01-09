package laogao.grpc

import laogao.grpc.hello.ZioHello.GreeterClient
import laogao.grpc.hello.GreeterServer
import laogao.grpc.hello.HelloRequest
import io.grpc.{ManagedChannelBuilder, Metadata}
import zio._
import zio.console._
import zio.logging._
import scalapb.zio_grpc.{SafeMetadata, ZManagedChannel}

object Main extends zio.App {

  val channel: ManagedChannelBuilder[_] = ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
  val clientManaged = GreeterClient.managed(ZManagedChannel(channel))

  val logging =
    Logging.console(
      logLevel = LogLevel.Debug,
      format = LogFormat.ColoredLogFormat()
    ) >>> Logging.withRootLoggerName("zio-grpc-demo")

  def myAppLogic = for {
    r1 <- clientManaged.use(client => client
      .withTimeoutMillis(1000)
      .withMetadataM(SafeMetadata.fromMetadata {
        val headers = new Metadata()
        headers.put(Metadata.Key.of("user-token", Metadata.ASCII_STRING_MARSHALLER), "aegon")
        headers
      })
      .sayHello(HelloRequest("Jon"))
      .mapError(_.asRuntimeException()))
    _ <- log.debug(s"gRPC call has returned: ${r1.message}")
    _ <- putStrLn("Greeter server should be running now. You can verify via grpcurl:")
    _ <- putStrLn("$ grpcurl -plaintext -d '{\"name\": \"jon\"}' -H 'user-token: aegon' localhost:9000 laogao.grpc.Greeter/SayHello")
    _ <- putStrLn("Or on windows:\n> grpcurl -plaintext -d {\\\"name\\\":\\\"jon\\\"}' -H user-token:aegon localhost:9000 laogao.grpc.Greeter/SayHello")
    _ <- putStrLn("Press any key to exit...")
    _ <- log.debug("waiting for user input...") *> getStrLn
  } yield ()

  // by providing GreeterServer this way we don't have to manually (re)start it for quick demo
  final def run(args: List[String]) =
    myAppLogic.provideCustomLayer(GreeterServer.serverLive ++ logging).exitCode

}