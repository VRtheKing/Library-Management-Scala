name := "library-manager"
organization := "com.library"
version := "1.0-SNAPSHOT"

scalaVersion := "2.13.16"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
enablePlugins(JavaAppPackaging, DockerPlugin)

val pekkoVersion = "1.1.5"
libraryDependencies ++= Seq(
  guice,
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,
  "com.typesafe.slick" %% "slick" % "3.6.1",
  "com.typesafe.play" %% "play-slick" % "5.4.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.4.0",
  "org.postgresql" % "postgresql" % "42.7.7",
  "com.typesafe.play" %% "filters-helpers" % "2.8.22"
)

dependencyOverrides ++= Seq(
  "org.apache.pekko" %% "pekko-actor" % pekkoVersion,
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
  "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
  "org.apache.pekko" %% "pekko-serialization-jackson" % pekkoVersion,
  "org.apache.pekko" %% "pekko-protobuf-v3" % pekkoVersion
)

libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % "1.75.0",
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,

  specs2 % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.mockito" %% "mockito-scala-scalatest" % "2.0.0" % Test

)
dependencyOverrides += "com.google.guava" % "guava" % "33.4.8-jre"

Compile / PB.targets := Seq(
  scalapb.gen(grpc = true) -> (Compile / sourceManaged).value / "scalapb"
)

// Optional: Clean up Twirl and Routes if needed later
// TwirlKeys.templateImports += "com.library.controllers._"
// play.sbt.routes.RoutesKeys.routesImport += "com.library.binders._"
