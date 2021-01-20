import com.github.mlangc.slf4zio.api._
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}
import zio.duration.durationInt
import zio.{IO, Schedule, Task, UIO, ZIO, ZManaged}

object JedisDemo extends zio.App with LoggingSupport {

  val poolConfig = new JedisPoolConfig()
  poolConfig.setMaxTotal(128)
  val jedisPool = new JedisPool(poolConfig,"localhost", 6379, 500, "")

  def release: Jedis => UIO[Unit] = (resource => Task(resource.close()).catchAll(err => logger.errorIO(err.toString) *> UIO(())))
  val jedisManaged = ZManaged.make(Task(jedisPool.getResource))(release)

  val program = for {
    _ <- logger.infoIO("redis set...")
    _ <- jedisManaged.use(client => ZIO(client.set("MY.KEY.01", "[]")))
    _ <- jedisManaged.use(client => ZIO(client.set("MY.KEY.02", "{}")))
    r1 <- jedisManaged.use(client => ZIO(client.get("MY.KEY.01")))
    r2 <- jedisManaged.use(client => ZIO(client.get("MY.KEY.02")))
    _ <- logger.infoIO(s"01:$r1 02:$r2")
  } yield ()

  val exec = for {
    _ <- program.catchAll(err => logger.errorIO("program >> " + err.toString)).repeat(Schedule.spaced(100.millisecond)).fork
    _ <- program.catchAll(err => logger.errorIO("program >> " + err.toString)).repeat(Schedule.spaced(100.millisecond)).fork
    _ <- program.catchAll(err => logger.errorIO("program >> " + err.toString)).repeat(Schedule.spaced(100.millisecond)).fork
    _ <- program.catchAll(err => logger.errorIO("program >> " + err.toString)).repeat(Schedule.spaced(100.millisecond)).fork
    _ <- ZIO.succeed(42).forever
  } yield ()

  final def run(args: List[String]) =
    exec.exitCode
}
