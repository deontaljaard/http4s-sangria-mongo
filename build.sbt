import Dependencies._
import sbt.Keys._
import sbt._
import sbtdocker.DockerPlugin.autoImport._

name := "http4s-sangria-mongo"

val port: Int = sys.props.getOrElse("PORT", "8080").toInt


lazy val dockerSettings = Seq(
  dockerfile in docker := {
    // The assembly task generates a fat JAR file
    val artifact: File = assembly.value
    val artifactTargetPath = s"/app/${artifact.name}"

    new Dockerfile {
      from("openjdk:8")
      add(artifact, artifactTargetPath)
      expose(port)
      entryPoint("java", "-jar", artifactTargetPath)
    }
  },
  imageNames in docker := {
    val dockerRegistryNamespace: String = if (sys.props.getOrElse("dockerRegistry", "aws") == "local")
      s"${organization.value}"
    else {
      val ecrNamespace = sys.props.getOrElse("ECR_NAMESPACE", "<YOUR ECR REPO URL>")
      s"$ecrNamespace/${organization.value}"
    }

    Seq(
      ImageName(
        namespace = Some(dockerRegistryNamespace),
        repository = name.value,
        tag = Some(version.value)
      )
    )
  }
)

lazy val loginAwsEcr = TaskKey[Unit]("loginAwsEcr", "Login AWS ECR")
loginAwsEcr := {
  import sys.process._
  val dockerLogin = Seq("aws", "ecr", "get-login", "--no-include-email", "--region", "eu-central-1", "--profile", "qa-wd").!!
  dockerLogin.replaceAll("\n", "").split(" ").toSeq.!
}

lazy val publishDocker = ReleaseStep(action = state => {
  val extracted = Project.extract(state)
  val ref: ProjectRef = extracted.get(thisProjectRef)
  extracted.runAggregated(loginAwsEcr in ref, state)
  extracted.runAggregated(
    sbtdocker.DockerKeys.dockerBuildAndPush in sbtdocker.DockerPlugin.autoImport.docker in ref,
    state)
  state
})

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  publishDocker,
  setNextVersion,
  commitNextVersion
)

lazy val model = (project in file("model"))
  .settings(
    Commons.settings,
    libraryDependencies ++= modelDependencies
  )

//lazy val core = (project in file("core"))
//  .settings(
//    Commons.settings,
//    libraryDependencies ++= coreDependencies,
//  ).dependsOn(model % "compile->compile;test->test")

lazy val service = (project in file("service"))
  .settings(
    Commons.settings,
    libraryDependencies ++= serviceDependencies,
    mainClass in assembly := Some("server.Http4sSangriaMongoServer"),
    dockerSettings
  ).dependsOn(model % "compile->compile;test->test")
  .enablePlugins(DockerPlugin)

lazy val root = (project in file("."))
  .aggregate(model, /*core,*/ service)
  .settings(
    Commons.settings
  )

addCommandAlias("dockerize", ";clean;assembly;service/docker")