ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.16"

ThisBuild / name := "pingrpc"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.7"

libraryDependencies += "org.openjfx" % "javafx" % "23.0.2"
libraryDependencies += "org.openjfx" % "javafx-controls" % "23.0.2"

libraryDependencies += "io.github.mkpaz" % "atlantafx-base" % "2.0.1"

libraryDependencies += "org.fxmisc.richtext" % "richtextfx" % "0.11.5"

libraryDependencies += "io.grpc" % "grpc-core" % "1.71.0"
libraryDependencies += "io.grpc" % "grpc-netty-shaded" % "1.71.0"
libraryDependencies += "io.grpc" % "grpc-protobuf" % "1.71.0"
libraryDependencies += "io.grpc" % "grpc-stub" % "1.71.0"
libraryDependencies += "io.grpc" % "grpc-services" % "1.71.0"
libraryDependencies += "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion

libraryDependencies += "com.google.protobuf" % "protobuf-java-util" % "3.25.7"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.19"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"

libraryDependencies += "io.circe" %% "circe-core" % "0.14.12"
libraryDependencies += "io.circe" %% "circe-generic" % "0.14.12"
libraryDependencies += "io.circe" %% "circe-parser" % "0.14.12"

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
  PB.gens.java -> (Compile / sourceManaged).value,
  scalapb.gen(javaConversions=true) -> (Compile / sourceManaged).value
)
