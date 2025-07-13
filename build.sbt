ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.16"

ThisBuild / name := "pingrpc"

PB.protocVersion := "4.31.1"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.6.2"

libraryDependencies += "org.openjfx" % "javafx" % "24.0.1"
libraryDependencies += "org.openjfx" % "javafx-controls" % "24.0.1"

libraryDependencies += "io.github.mkpaz" % "atlantafx-base" % "2.1.0"

libraryDependencies += "org.fxmisc.richtext" % "richtextfx" % "0.11.5"

libraryDependencies += "io.grpc" % "grpc-core" % "1.73.0"
libraryDependencies += "io.grpc" % "grpc-netty-shaded" % "1.73.0"
libraryDependencies += "io.grpc" % "grpc-protobuf" % "1.73.0"
libraryDependencies += "io.grpc" % "grpc-stub" % "1.73.0"
libraryDependencies += "io.grpc" % "grpc-services" % "1.73.0"
libraryDependencies += "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion

libraryDependencies += "com.google.protobuf" % "protobuf-java-util" % PB.protocVersion.value

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.19"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.18"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"

libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
libraryDependencies += "com.google.protobuf" % "protobuf-java" % PB.protocVersion.value % "protobuf"

libraryDependencies += "io.circe" %% "circe-core" % "0.14.14"
libraryDependencies += "io.circe" %% "circe-generic" % "0.14.14"
libraryDependencies += "io.circe" %% "circe-parser" % "0.14.14"

libraryDependencies += "net.harawata" % "appdirs" % "1.4.0"

enablePlugins(JavaAppPackaging)
enablePlugins(LauncherJarPlugin)

assemblyMergeStrategy in assembly := {
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case x if x.endsWith("LoadBalancerProvider") => MergeStrategy.concat
  case x if x.startsWith("META-INF") => MergeStrategy.discard
  case x => MergeStrategy.first
}

Compile / PB.targets := Seq(
  PB.gens.java("4.31.1") -> (Compile / sourceManaged).value,
  scalapb.gen(javaConversions = true) -> (Compile / sourceManaged).value
)
