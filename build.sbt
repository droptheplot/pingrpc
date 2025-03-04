ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "2.13.16"

ThisBuild / name := "pingrpc"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.7"

libraryDependencies += "org.openjfx" % "javafx" % "23.0.2"
libraryDependencies += "org.openjfx" % "javafx-controls" % "23.0.2"

libraryDependencies += "io.grpc" % "grpc-core" % "1.71.0"
libraryDependencies += "io.grpc" % "grpc-netty-shaded" % "1.71.0"
libraryDependencies += "io.grpc" % "grpc-protobuf" % "1.71.0"
libraryDependencies += "io.grpc" % "grpc-stub" % "1.71.0"
libraryDependencies += "io.grpc" % "grpc-services" % "1.71.0"

libraryDependencies += "com.google.protobuf" % "protobuf-java-util" % "4.30.0"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.19"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

enablePlugins(JavaAppPackaging)
enablePlugins(LauncherJarPlugin)

assemblyMergeStrategy in assembly := {
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case x if x.endsWith("LoadBalancerProvider") => MergeStrategy.concat
  case x if x.startsWith("META-INF") => MergeStrategy.discard
  case x => MergeStrategy.first
}
