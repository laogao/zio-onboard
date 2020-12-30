package laogao.bidi

import io.grpc.Status
import zio.{Has, Schedule, ZEnv, ZIO, ZLayer, ZManaged}
import laogao.bidi.bidi.ZioBidi.ZPipe
import laogao.bidi.bidi.SmokeRequest
import laogao.bidi.bidi.SmokeResponse
import zio.stream.{Stream, ZSink, ZStream}
import scalapb.zio_grpc.ServerMain
import scalapb.zio_grpc.ServiceList
import zio.duration.durationInt
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.consumer._
import zio.kafka.serde._
import zio.console._

object PipeServerWithKafka extends ServerMain {

  object PipeServerImpl extends ZPipe[ZEnv, Any] {
    def smoke(request: zio.stream.Stream[Status, SmokeRequest]): ZStream[ZEnv, Status, SmokeResponse] = {
      val relay = request.filter("1" == _.message).flatMap(req => {
        val consumerSettings: ConsumerSettings = ConsumerSettings(List("10.0.0.8:9092")).withGroupId("group")
        val consumerManaged: ZManaged[Clock with Blocking, Throwable, Consumer.Service] = Consumer.make(consumerSettings)
        val consumer: ZLayer[Clock with Blocking, Throwable, Consumer] =  ZLayer.fromManaged(consumerManaged)
        Consumer.subscribeAnd(Subscription.topics("poc"))
          .plainStream(Serde.string, Serde.string)
          .provideCustomLayer(consumer)
          .map(r => SmokeResponse(r.record.value))
          .mapError(err => io.grpc.Status.UNKNOWN)
      })
      val ack = request.filter("2" == _.message).flatMap(req =>
        ZStream.fromEffect(ZIO.succeed(SmokeResponse(s"[ACK: ${req.message}]"))))
      ZStream.mergeAll(2)(relay, ack)
    }
  }

  def services: ServiceList[ZEnv] = ServiceList.add(PipeServerImpl)

}