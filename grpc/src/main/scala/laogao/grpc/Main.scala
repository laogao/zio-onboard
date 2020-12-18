package laogao.grpc

import laogao.grpc.hello.ZioHello.GreeterClient
import laogao.grpc.hello.GreeterServer
import laogao.grpc.hello.HelloRequest
import io.grpc.ManagedChannelBuilder
import zio.console._
import scalapb.zio_grpc.ZManagedChannel
import zio.Layer

object Main extends zio.App {
  
  val clientLayer: Layer[Throwable, GreeterClient] =
    GreeterClient.live(
      ZManagedChannel(
        ManagedChannelBuilder.forAddress("localhost", 9000).usePlaintext()
      )
    )

  def myAppLogic =
    for {
      // TODO figure out how to add metadata in request (grpcurl works as such: grpcurl -plaintext -d '{"name": "jon"}' -H 'user-token: aegon' localhost:9000 laogao.grpc.Greeter/SayHello)
      //r1 <- GreeterClient.sayHello(HelloRequest("Jon"))
      //_ <- putStrLn(r1.message)
      //r2 <- GreeterClient.sayHelloTwice(HelloRequest("Jon"))
      //_ <- putStrLn(r2.message)
      _ <- putStrLn("Greeter server should be running now. You can verify via grpcurl:")
      _ <- putStrLn("$ grpcurl -plaintext -d '{\"name\": \"jon\"}' -H 'user-token: aegon' localhost:9000 laogao.grpc.Greeter/SayHello)")
      _ <- getStrLn
    } yield ()

  // by providing GreeterServer this way we don't have to manually (re)start it for quick demo
  final def run(args: List[String]) =
    myAppLogic.provideCustomLayer(GreeterServer.serverLive ++ clientLayer).exitCode
}