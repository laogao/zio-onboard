val zioGrpcVersion = "0.4.0"

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.0-RC2")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.4")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.8"
libraryDependencies += "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % zioGrpcVersion
