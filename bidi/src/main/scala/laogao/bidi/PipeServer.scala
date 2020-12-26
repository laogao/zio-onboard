package laogao.bidi

import io.grpc.Status
import zio.{Schedule, ZEnv, ZIO}
import laogao.bidi.bidi.ZioBidi.ZPipe
import laogao.bidi.bidi.SmokeRequest
import laogao.bidi.bidi.SmokeResponse
import zio.stream.{Stream, ZStream}
import scalapb.zio_grpc.ServerMain
import scalapb.zio_grpc.ServiceList
import zio.duration.durationInt

object PipeServer extends ServerMain {

  object PipeServerImpl extends ZPipe[ZEnv, Any] {
    def smoke(request: zio.stream.Stream[Status, SmokeRequest]): ZStream[ZEnv, Status, SmokeResponse] = {
      ZStream.mergeAll(2)(
        request.flatMap(req => ZStream.fromEffect(ZIO.succeed(SmokeResponse(s"[ACK: ${req.message}]")))),
        ZStream.repeatEffectWith(ZIO.succeed(SmokeResponse(s"${new java.util.Date()}")), Schedule.spaced(1.seconds))
      )
    }
  }

  def services: ServiceList[ZEnv] = ServiceList.add(PipeServerImpl)

}