package model.mongo

import model.config.Http4sSangriaAppConfig
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.core.errors.ConnectionException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.control.NonFatal

object ReactiveClientBuilder {

  private implicit val timeout = 10 seconds
  private val reactiveUri = Http4sSangriaAppConfig.DbConfig.reactiveUri

  val driver = MongoDriver()

  val eventualConnection: Future[MongoConnection] = if (reactiveUri.contains("localhost")) {
    Future(driver.connection(List(reactiveUri)))
  } else {
    Future.fromTry(driver.connection(reactiveUri))
  }

  private def getDatabase(databaseName: String): Future[DefaultDB] = eventualConnection.flatMap(_.database(databaseName))

  def collectionFromDataBase(databaseName: String, collectionName: String): Future[BSONCollection] =
    getDatabase(databaseName)
      .map(_.collection(collectionName))
      .recoverWith {
        case NonFatal(e) =>
          Future.failed(ConnectionException(s"Connection cannot be established. Reason: ${e.getMessage}"))
      }

  def close(): Future[Unit] =
    eventualConnection.map(_.askClose())
}


