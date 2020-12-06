ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "laogao"
ThisBuild / version      := "1.0"

val Http4sVersion     = "0.21.8"
val CirceVersion      = "0.13.0"
val DoobieVersion     = "0.9.2"
val ZIOVersion        = "1.0.3"
val PureConfigVersion = "0.14.0"
val ZIOInteropVersion = "2.2.0.1"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
)

lazy val doobie = (project in file("doobie"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "doobie",
    libraryDependencies ++= Seq(
      // ZIO
      "dev.zio" %% "zio"              % ZIOVersion,
      "dev.zio" %% "zio-interop-cats" % ZIOInteropVersion,
      "dev.zio" %% "zio-test"         % ZIOVersion % "test",
      "dev.zio" %% "zio-test-sbt"     % ZIOVersion % "test",
      // Http4s
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,
      // Circe
      "io.circe" %% "circe-generic"        % CirceVersion,
      "io.circe" %% "circe-generic-extras" % CirceVersion,
      // Doobie
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-h2"   % DoobieVersion,
      // pureconfig
      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      // log
      "org.slf4j" % "slf4j-log4j12" % "1.7.30"    
    )
  )

lazy val kafka = (project in file("kafka"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "kafka",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-streams" % "1.0.2",
      "dev.zio" %% "zio-kafka" % "0.13.0",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.11.3"
    )
  )

lazy val layers = (project in file("layers"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "layers",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % ZIOVersion
    )
  )
