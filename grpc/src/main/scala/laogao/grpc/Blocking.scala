package laogao.grpc

import com.github.mlangc.slf4zio.api.{LoggingSupport, _}
import io.grpc.{ManagedChannelBuilder, Metadata}
import laogao.grpc.hello.{GreeterServer, HelloRequest}
import laogao.grpc.hello.ZioHello.GreeterClient
import scalapb.zio_grpc.{SafeMetadata, ZManagedChannel}
import zio.ZIO
import zio.blocking.blocking
import zio.console._

object Blocking extends App with LoggingSupport {

  zio.Runtime.global.unsafeRunSync(
    blocking(logger.infoIO("which thread pool?")).fork *>
      blocking(logger.infoIO("which thread pool?")).fork *>
      blocking(logger.infoIO("which thread pool?")).fork *>
      blocking(logger.infoIO("which thread pool?")).fork
  )
  zio.Runtime.default.unsafeRunSync(
    blocking(logger.infoIO("which thread pool?")).fork *>
      blocking(logger.infoIO("which thread pool?")).fork *>
      blocking(logger.infoIO("which thread pool?")).fork *>
      blocking(logger.infoIO("which thread pool?")).fork
  )
  zio.Runtime.global.unsafeRunSync((logger.infoIO("which thread pool?")))
  zio.Runtime.default.unsafeRunSync((logger.infoIO("which thread pool?")))

}