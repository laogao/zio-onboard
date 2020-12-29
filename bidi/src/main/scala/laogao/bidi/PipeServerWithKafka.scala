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
import zio.ZLayer
import zio.ZManaged
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.consumer._
import zio.kafka.serde._
import zio.console._

object PipeServerWithKafka extends ServerMain {

  object PipeServerImpl extends ZPipe[ZEnv, Any] {
    def smoke(request: zio.stream.Stream[Status, SmokeRequest]): ZStream[ZEnv, Status, SmokeResponse] = {
      val consumerSettings: ConsumerSettings = ConsumerSettings(List("10.0.0.8:9092")).withGroupId("group")
      val consumerManaged: ZManaged[Clock with Blocking, Throwable, Consumer.Service] = Consumer.make(consumerSettings)
      val consumer: ZLayer[Clock with Blocking, Throwable, Consumer] =  ZLayer.fromManaged(consumerManaged)
      val kafkaStream: ZStream[ZEnv, Status, SmokeResponse] = Consumer.subscribeAnd(Subscription.topics("poc"))
        .plainStream(Serde.string, Serde.string)
        .provideSomeLayer[ZEnv](consumer)
        .map(r => SmokeResponse(r.record.value))
        .mapError(err => io.grpc.Status.UNKNOWN)
      ZStream.mergeAll(2)(
        request.flatMap(req => ZStream.fromEffect(ZIO.succeed(SmokeResponse(s"[ACK: ${req.message}]")))),
        kafkaStream
      )
    }
  }

  def services: ServiceList[ZEnv] = ServiceList.add(PipeServerImpl)

}