package laogao.kafka

import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.duration.durationInt
import zio.kafka.producer._
import zio.kafka.serde._

object Hello extends zio.App {

  //val consumerSettings: ConsumerSettings = ConsumerSettings(List("10.0.0.8:9092")).withGroupId("group")
  val producerSettings: ProducerSettings = ProducerSettings(List("10.0.0.8:9092"))

  val produceManual = for {
    res <- Producer.produce[Any, Int, String]("pocs", 1, new java.util.Date().toString)
    _ <- console.putStrLn(res.toString)
  } yield res

  val dateStringProducer = ZLayer.fromManaged(Producer.make(producerSettings, Serde.int, Serde.string))

  override def run(args: List[String]) = {
    produceManual
      .repeat(Schedule.spaced(10.milliseconds)).orDie
      .provideSomeLayer(Blocking.live ++ dateStringProducer ++ Console.live ++ Clock.live)
      .exitCode
  }
}
