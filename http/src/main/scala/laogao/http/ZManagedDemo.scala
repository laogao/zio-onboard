package laogao.http

object ZManagedDemo extends App {
  val sampleManaged = zio.ZManaged.make(zio.console.putStrLn(">>acquiring") *> zio.ZIO.succeed(42))(_ => zio.console.putStrLn("releasing<<"))
  zio.Runtime.default.unsafeRun(sampleManaged.use(_ => zio.console.putStrLn("[using]")))
  zio.Runtime.default.unsafeRun(sampleManaged.use(_ => zio.console.putStrLn("[using]")))
  zio.Runtime.default.unsafeRun(sampleManaged.use(_ => zio.console.putStrLn("*using*")) *> sampleManaged.use(_ => zio.console.putStrLn("*using*")))
}