ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.5"

lazy val root = (project in file("."))
  .settings(
    name := "News Processor",
    libraryDependencies ++= commonDependencies
  )
  .aggregate(
    newsFetcher
  )

lazy val newsFetcher = (project in file("news-fetcher"))
  .settings(
    name := "News Fetcher",
    libraryDependencies ++= commonDependencies
  )

// DEPENDENCIES

lazy val dependencies =
  new {
    val zioV = "1.0.4-2"
    val zioActorsV = "0.0.9"
    val typesafeConfigV = "1.4.1"
    val akkaVersion = "2.6.13"
    val logbackVersion = "1.2.3"

    val zio = "dev.zio" %% "zio" % zioV
    val zioActors = "dev.zio" %% "zio-actors" % zioActorsV
    val typesafeConfig = "com.typesafe" % "config" % typesafeConfigV
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
    val akkaActors = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
    val logback = "ch.qos.logback" % "logback-classic" % logbackVersion

    val akkaActorsTestkit = "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
  }

lazy val commonDependencies = Seq(
  dependencies.zio,
  dependencies.zioActors,
  dependencies.typesafeConfig,
  dependencies.akkaStream,
  dependencies.akkaActors,
  dependencies.akkaSlf4j,
  dependencies.logback
)