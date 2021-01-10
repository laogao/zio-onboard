package laogao.etcd

import zio._
import zio.console.{Console, putStrLn}
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import io.etcd.jetcd.kv.GetResponse
import io.etcd.jetcd.options.GetOption
import com.google.common.base.Charsets.UTF_8
import zio.duration.durationInt

object Main extends zio.App {

  val jetcdManaged = zio.ZManaged.make(
    zio.console.putStrLn(">>acquiring") *>
    zio.Task{Client.builder().endpoints("http://localhost:23790").build()})(resource =>
    zio.console.putStrLn(s"releasing $resource<<") *> zio.Task(resource.close()).ignore
  )

  // TODO wrap jetcd client (kv) into a service

  val program = for {
    _ <- jetcdManaged.use { client => Task { client.getKVClient().put(ByteSequence.from("zio".getBytes()),ByteSequence.from("ftw1".getBytes())).get() } }
    _ <- jetcdManaged.use { client => Task { client.getKVClient().put(ByteSequence.from("zio".getBytes()),ByteSequence.from("ftw2".getBytes())).get() } }
    _ <- jetcdManaged.use { client => putStrLn("" + client.getKVClient().get(ByteSequence.from("zio".getBytes())).get().getKvs().get(0).getValue().toString(UTF_8)) }
  } yield ()
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode

}