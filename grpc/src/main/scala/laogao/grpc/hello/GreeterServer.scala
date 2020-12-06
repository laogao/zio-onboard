package laogao.grpc.hello

import io.grpc.Status
import zio.{ZEnv, ZIO}
import zio.console._
import laogao.grpc.hello.ZioHello.ZGreeter

object GreeterImpl extends ZGreeter[ZEnv, Any] {
  def sayHello(request: HelloRequest): ZIO[zio.ZEnv, Status, HelloReply] =
    putStrLn(s"Got request: $request") *> ZIO.succeed(HelloReply(s"Hello, ${request.name}!"))
  def sayHelloTwice(request: HelloRequest): ZIO[zio.ZEnv, Status, HelloReply] =
    putStrLn(s"Got request: $request") *> ZIO.succeed(HelloReply(s"Hello, and hello, ${request.name}!!"))
}

import scalapb.zio_grpc.ServerMain
import scalapb.zio_grpc.ServiceList

object GreeterServer extends ServerMain {
  def services: ServiceList[zio.ZEnv] = ServiceList.add(GreeterImpl)
}