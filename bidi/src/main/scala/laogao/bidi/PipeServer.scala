package laogao.bidi

import io.grpc.Status
import zio.{Has, IO, ZEnv, ZIO}
import zio.console._
import laogao.bidi.bidi.ZioBidi.ZPipe
import laogao.bidi.bidi.SmokeRequest
import laogao.bidi.bidi.SmokeResponse
import zio.stream.{Stream, ZStream}
import scalapb.zio_grpc.ServerMain
import scalapb.zio_grpc.ServiceList

object PipeServer extends ServerMain {

  object PipeServerImpl extends ZPipe[ZEnv, Any] {
    // TODO implement server logic
    def smoke(request: zio.stream.Stream[Status, SmokeRequest]): ZStream[ZEnv, Status, SmokeResponse] = ???
  }

  def services: ServiceList[zio.ZEnv] = ServiceList.add(PipeServerImpl)

}