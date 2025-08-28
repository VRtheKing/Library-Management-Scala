name := """library-manager"""
organization := "com.library"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.6.1"
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.4.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.4.0"
)
libraryDependencies += "org.postgresql" % "postgresql" % "42.7.7"
libraryDependencies += "com.typesafe.play" %% "filters-helpers" % "2.8.22"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.library.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.library.binders._"
