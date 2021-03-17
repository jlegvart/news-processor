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
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.jsoup,
      dependencies.commonsLang3
    )
  )

// DEPENDENCIES

lazy val dependencies =
  new {
    val zioVersion = "1.0.4-2"
    val zioActorsVersion = "0.0.9"
    val typesafeConfigVersion = "1.4.1"
    val logbackVersion = "1.2.3"
    val akkaVersion = "2.6.13"
    val akkaHttpVersion = "10.2.4"
    val jsoupVersion = "1.13.1"
    val jacksonVersion = "2.12.2"
    val commonsLang3Version = "3.12.0"
    val elasticClientVersion = "7.11.0"

    val scalaTestVersion = "3.0.8"

    val zio = "dev.zio" %% "zio" % zioVersion
    val zioActors = "dev.zio" %% "zio-actors" % zioActorsVersion
    val typesafeConfig = "com.typesafe" % "config" % typesafeConfigVersion
    val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
    val jsoup = "org.jsoup" % "jsoup" % jsoupVersion

    val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
    val akkaActors = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
    val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

    val jackson = "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
    val jacksonXml = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % jacksonVersion
    val jacksonScalaModule = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
    val jacksonJava8Module = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion

    val elasticClient =   "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" %  elasticClientVersion
    val elasticJackson =   "com.sksamuel.elastic4s" %% "elastic4s-json-jackson" %  elasticClientVersion

    val commonsLang3 = "org.apache.commons" % "commons-lang3" % commonsLang3Version

    val scalatest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test
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
  dependencies.jackson,
  dependencies.jacksonXml,
  dependencies.jacksonScalaModule,
  dependencies.jacksonJava8Module,
  dependencies.elasticClient,
  dependencies.elasticJackson,
  dependencies.scalatest
)