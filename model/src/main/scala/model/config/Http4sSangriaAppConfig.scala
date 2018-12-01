package model.config

import com.typesafe.config.ConfigFactory

object Http4sSangriaAppConfig {
  private val config = ConfigFactory.load()

  object AppConfig {
    private val appConfig = config.getConfig("app")

    lazy val local: Boolean = appConfig.getBoolean("local")
  }

  object HttpConfig {
    private val httpConfig = config.getConfig("http")

    lazy val port: Int = httpConfig.getInt("port")
    lazy val host: String = httpConfig.getString("host")
    lazy val apiPrefix: String = httpConfig.getString("apiPrefix")
  }

  object DbConfig {
    private val dbConfig = config.getConfig("db")
    lazy val reactiveUri: String = dbConfig.getString("reactive.uri")

    private val personServiceConfig = dbConfig.getConfig("personService")
    lazy val personsDb: String = personServiceConfig.getString("db")

    lazy val pageSize: Int = dbConfig.getInt("pageSize")
    lazy val personsCollection: String = personServiceConfig.getString("personsCollection")
    lazy val devicesCollection: String = personServiceConfig.getString("devicesCollection")
  }

}
