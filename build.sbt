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
    val logbackVersion = "1.2.3"
    val akkaVersion = "2.6.13"
    val akkaHttpVersion = "10.2.4"

    val zio = "dev.zio" %% "zio" % zioV
    val zioActors = "dev.zio" %% "zio-actors" % zioActorsV
    val typesafeConfig = "com.typesafe" % "config" % typesafeConfigV
    val logback = "ch.qos.logback" % "logback-classic" % logbackVersion

    val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
    val akkaActors = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
    val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

    val jacksonCore = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % "2.12.2"
    val jacksonModule = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2"

    val akkaActorsTestkit = "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
  }

lazy val commonDependencies = Seq(
  dependencies.zio,
  dependencies.zioActors,
  dependencies.typesafeConfig,
  dependencies.logback,
  dependencies.akkaStream,
  dependencies.akkaHttp,
  dependencies.akkaActors,
  dependencies.akkaSlf4j,
  dependencies.jacksonCore,
  dependencies.jacksonModule
)