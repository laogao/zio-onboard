package laogao.etcd

import zio._
import zio.console.putStrLn
import io.etcd.jetcd.ByteSequence
import io.etcd.jetcd.Client
import com.google.common.base.Charsets.UTF_8
import zio.duration.durationInt

object Main extends zio.App {

  // thin jetcd wrapper
  val jetcdManaged = zio.ZManaged.make(
    zio.console.putStrLn(">>acquiring thin jetcd wrapper") *>
    zio.Task(Client.builder().endpoints("http://localhost:23790").build()))(resource =>
    zio.console.putStrLn(s"releasing $resource<<") *> zio.Task(resource.close()).ignore
  )

  // simple reader
  trait EtcdReader {
    def get(key: String): String // return "" if not present
    def close(): Unit
  }

  object EtcdReader {
    trait _EtcdReader extends EtcdReader {
      var client: io.etcd.jetcd.Client
    }
    def make(endpoints: String, group: String): EtcdReader = {
      if (group == null || group.isBlank) throw new IllegalArgumentException()
      val c = Client.builder().endpoints(endpoints).build()
      new _EtcdReader {
        override var client: Client = c
        override def get(key: String): String = {
          var res = ""
          try {
            res = c.getKVClient.get(ByteSequence.from(s"$group/$key".getBytes())).get().getKvs.get(0).getValue.toString(UTF_8)
          } catch {
            case ex: Throwable =>
              // ignored
          }
          res
        }
        override def close(): Unit = c.close()
      }
    }
  }

  val etcdReaderManaged = zio.ZManaged.make(
    zio.console.putStrLn(">>acquiring EtcdReader") *>
    zio.Task(EtcdReader.make("http://localhost:23790", "demo")))(resource =>
    zio.console.putStrLn(s"releasing $resource<<") *> zio.Task(resource.close()).ignore
  )

  val program = for {
    _ <- jetcdManaged.use(client => Task(client.getKVClient().put(ByteSequence.from("zio".getBytes()),ByteSequence.from("ftw1".getBytes())).get()))
    _ <- jetcdManaged.use(client => Task(client.getKVClient().put(ByteSequence.from("zio".getBytes()),ByteSequence.from("ftw2".getBytes())).get()))
    _ <- jetcdManaged.use(client => Task(client.getKVClient().put(ByteSequence.from("demo/foo".getBytes()),ByteSequence.from("bar".getBytes())).get()))
    _ <- jetcdManaged.use(client => Task(client.getKVClient().put(ByteSequence.from("demo/foo".getBytes()),ByteSequence.from("baz".getBytes())).get()))
    _ <- putStrLn("[thin wrapper approach]")
    _ <- jetcdManaged.use(client => putStrLn("" + client.getKVClient().get(ByteSequence.from("zio".getBytes())).get().getKvs().get(0).getValue().toString(UTF_8)))
    _ <- ZIO.sleep(1000.millisecond)
    _ <- putStrLn("[reader approach]")
    _ <- etcdReaderManaged.use(reader => putStrLn(reader.get("foo")))
  } yield ()
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.exitCode

}