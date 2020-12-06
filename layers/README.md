ZIO Layers

```
import zio.{Has, UIO, ZIO, ZLayer}

object LayersExample {

  type ServiceA = Has[ServiceA.Service]
  object ServiceA {
    trait Service {
      def a(): UIO[Unit]
    }
    def a(): ZIO[ServiceA, Nothing, Unit] = ZIO.accessM[ServiceA](_.get.a())
    val default = ZLayer.succeed(new Service { override def a(): UIO[Unit] = ??? })
  }

  type ServiceB = Has[ServiceB.Service]
  object ServiceB {
    trait Service {
      def b(): UIO[Unit]
    }
    def b(): ZIO[ServiceB, Nothing, Unit] = ZIO.accessM[ServiceB](_.get.b())
    val default = ZLayer.succeed(new Service { override def b(): UIO[Unit] = ??? })
  }

  type ServiceC = Has[ServiceC.Service]
  object ServiceC {
    trait Service {
      def c(): UIO[Unit]
    }
    def c(): ZIO[ServiceC, Nothing, Unit] = ZIO.accessM[ServiceC](_.get.c())
    val default = ZLayer.succeed(new Service { override def c(): UIO[Unit] = ??? })
  }

  // horizontal
  val ha: ZLayer[Any, Nothing, ServiceA] = ???
  val hb: ZLayer[Any, Nothing, ServiceB] = ???
  val hc: ZLayer[Any, Nothing, ServiceC] = ???
  val h = ha ++ hb ++ hc

  // vertical
  val va: ZLayer[Any, Nothing, ServiceA] = ???
  val vb: ZLayer[ServiceA, Nothing, ServiceB] = ???
  val vc: ZLayer[ServiceB, Nothing, ServiceC] = ???
  val v = va >>> vb >>> vc

  // vertical pass-through
  val za: ZLayer[Any, Nothing, ServiceA] = ???
  val zb: ZLayer[ServiceA, Nothing, ServiceB] = ???
  val zc: ZLayer[ServiceA with ServiceB, Nothing, ServiceC] = ???
  val z = za >+> zb >>> zc

  // hybrid
  val hya: ZLayer[Any, Nothing, ServiceA] = ???
  val hyb: ZLayer[Any, Nothing, ServiceB] = ???
  val hyc: ZLayer[ServiceA with ServiceB, Nothing, ServiceC] = ???
  val hy = hya ++ hyb >>> hyc

  // order within same level is insignificant
  val hy1 = hya ++ hyb >>> hyc
  val hy2 = hyb ++ hya >>> hyc

}
```