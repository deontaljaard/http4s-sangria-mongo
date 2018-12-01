name := "http4s-sangria-mongo"
lazy val appMainClass = Some("server.Http4sSangriaMongoServer")
mainClass in (Compile, run) := appMainClass
mainClass in (Compile, packageBin) := appMainClass