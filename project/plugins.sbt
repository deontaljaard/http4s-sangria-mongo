lazy val sbtAssembly = "com.eed3si9n" % "sbt-assembly" % "0.14.6"
lazy val sbtRelease = "com.github.gseitz" % "sbt-release" % "1.0.7"
lazy val sbtDocker = "se.marcuslonnberg" % "sbt-docker" % "1.5.0"

addSbtPlugin(sbtAssembly)
addSbtPlugin(sbtRelease)
addSbtPlugin(sbtDocker)