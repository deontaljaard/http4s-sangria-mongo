import sbt._

object Dependencies {
  val catsCoreVersion = "1.1.0"
  val catsEffectVersion = "0.10"
  val fs2IOVersion = "0.10.3"
  val http4sVersion = "0.18.20"
  val circeVersion = "0.9.3"
  val sangriaGraphQLVersion = "1.4.2"
  val sangriaCirceVersion = "1.2.1"
  val scalaLoggingVersion = "3.7.2"
  val logbackClassicVersion = "1.2.3"
  val reactiveMongoScalaVersion = "0.15.0"
  val specs2Version = "4.0.2"
  val scalaCheckVersion = "1.14.0"
  val typesafeConfigVersion = "1.3.2"
  val jodaTimeVersion = "2.10.1"

  // Compile Dependencies
  val catsCore: ModuleID = "org.typelevel" %% "cats-core" % catsCoreVersion
  val fs2IO: ModuleID = "co.fs2" %% "fs2-io" % fs2IOVersion

  val http4sBlazeServer: ModuleID = "org.http4s" %% "http4s-blaze-server" % http4sVersion
  val http4sCirce: ModuleID = "org.http4s" %% "http4s-circe" % http4sVersion
  val http4sDsl: ModuleID = "org.http4s" %% "http4s-dsl" % http4sVersion

  val circeCore: ModuleID = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric: ModuleID = "io.circe" %% "circe-generic" % circeVersion
  val circeParser: ModuleID = "io.circe" %% "circe-parser" % circeVersion
  val circeOptics: ModuleID = "io.circe" %% "circe-optics" % circeVersion

  val sangriaGraphQL: ModuleID = "org.sangria-graphql" %% "sangria" % sangriaGraphQLVersion
  val sangriaCirce: ModuleID = "org.sangria-graphql" %% "sangria-circe" % sangriaCirceVersion

  val typesafeConfig: ModuleID = "com.typesafe" % "config" % typesafeConfigVersion
  val scalaLogging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion

  val akkaSlf4j: ModuleID = "com.typesafe.akka" %% "akka-slf4j" % "2.5.13"
  val logbackCore: ModuleID = "ch.qos.logback" % "logback-core" % logbackClassicVersion
  val logbackClassic: ModuleID = "ch.qos.logback" % "logback-classic" % logbackClassicVersion

  val reactiveMongoScala: ModuleID = "org.reactivemongo" %% "reactivemongo" % reactiveMongoScalaVersion

  val jodaTime: ModuleID = "joda-time" % "joda-time" % jodaTimeVersion

  // Test dependencies
  val specs2Core: ModuleID = "org.specs2" %% "specs2-core" % specs2Version
  val specs2Mock: ModuleID = "org.specs2" %% "specs2-mock" % specs2Version
  val scalaCheck: ModuleID = "org.scalacheck" %% "scalacheck" % scalaCheckVersion

  // Module dependencies
  lazy val commonDependencies: Seq[ModuleID] = Seq(
    typesafeConfig,
    catsCore,
    fs2IO,
    scalaLogging,
    akkaSlf4j,
    logbackCore,
    logbackClassic,
    circeCore,
    circeGeneric,
    circeParser,
    jodaTime,
    specs2Core % "test",
    specs2Mock % "test",
    scalaCheck % "test"
  )

  lazy val modelDependencies: Seq[ModuleID] = Seq(
    reactiveMongoScala
  ) ++ commonDependencies

  //  lazy val coreDependencies: Seq[ModuleID] = Seq(
  //  ) ++ commonDependencies

  lazy val serviceDependencies: Seq[ModuleID] = Seq(
    http4sBlazeServer,
    http4sCirce,
    http4sDsl,
    sangriaGraphQL,
    sangriaCirce,
    circeOptics
  ) ++ commonDependencies
}
