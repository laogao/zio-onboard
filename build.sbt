ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "laogao"
ThisBuild / version      := "1.0"

val Http4sVersion     = "0.21.8"
val CirceVersion      = "0.13.0"
val DoobieVersion     = "0.9.2"
val ZIOVersion        = "1.0.3"
val PureConfigVersion = "0.14.0"
val ZIOInteropVersion = "2.2.0.1"
val grpcVersion       = "1.34.1"
val zioGrpcVersion    = "0.4.2"
val AkkaVersion       = "2.6.8"
val AkkaHttpVersion   = "10.2.2"

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
    ),
    run / fork := true
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
  .enablePlugins(DockerPlugin)
  .settings(
    name := "layers",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % ZIOVersion
    )
  )

lazy val grpc = (project in file("grpc"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(ProtocPlugin)
  .settings(
    name := "grpc",
    PB.targets in Compile := Seq(
      scalapb.gen(grpc = true) -> (sourceManaged in Compile).value,
      scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-streams" % ZIOVersion,
      "io.grpc" % "grpc-netty" % grpcVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.10.1",
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core" % zioGrpcVersion
    )
  )

lazy val sttp = (project in file("sttp"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "sttp",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"         % ZIOVersion,
      "dev.zio" %% "zio-streams" % ZIOVersion,
      "com.softwaremill.sttp.client3" %% "core" % "3.0.0-RC13",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.0.0-RC13",
      "com.softwaremill.sttp.client3" %% "circe" % "3.0.0-RC13",
      "io.circe" %% "circe-generic" % "0.13.0"
    )
  )

lazy val http = (project in file("http"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(ProtocPlugin)
  .settings(
    name := "http",
    PB.targets in Compile := Seq(
      scalapb.gen(grpc = true) -> (sourceManaged in Compile).value,
      scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"         % ZIOVersion,
      "dev.zio" %% "zio-streams" % ZIOVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "io.grpc" % "grpc-netty" % grpcVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.10.1",
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core" % zioGrpcVersion,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.0"
    )
  )

lazy val bidi = (project in file("bidi"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(ProtocPlugin)
  .settings(
    name := "bidi",
    PB.targets in Compile := Seq(
      scalapb.gen(grpc = true) -> (sourceManaged in Compile).value,
      scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % ZIOVersion,
      "dev.zio" %% "zio-streams" % ZIOVersion,
      "dev.zio" %% "zio-kafka" % "0.13.0",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.0",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.10.1",
      "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-core" % zioGrpcVersion,
      "io.grpc" % "grpc-netty" % grpcVersion
    )
  )

