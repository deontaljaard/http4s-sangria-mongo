package model.mongo

import com.typesafe.scalalogging.LazyLogging
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future._
import scala.reflect._
import scala.util.control.NonFatal

object ReactiveQueryHelper extends LazyLogging {

  val cleanClassName: String => String =
    className => className.replaceAll("\\$", "")

  //queries
  val byId: BSONObjectID => BSONDocument = objectId => document("_id" -> objectId)
  val byStringFields: Seq[(String, String)] => BSONDocument =
    fieldValuePairs => BSONDocument(
      fieldValuePairs.foldLeft(List.empty[(String, BSONValue)]) { case (acc, (field, value)) =>
        acc :+ (field -> BSONString(value))
      }
    )

  def findEntityById[A, B: ClassTag](id: String, eventualCollection: Future[BSONCollection],
                                     query: BSONObjectID => BSONDocument = byId)
                                    (aToB: A => B)
                                    (implicit resourceReader: BSONDocumentReader[A]): Future[Option[B]] = {
    val entityName = cleanClassName(classTag[B].runtimeClass.getSimpleName)
    logger.info(s"Looking for '$entityName' with id '$id'.")

    val eventualEntity = BSONObjectID.parse(id).map { objectId =>
      for {
        collection <- eventualCollection
        maybeEntity <- collection.find(query(objectId)).one[A]
      } yield maybeEntity.map(aToB)
    }.recover {
      case iae: IllegalArgumentException =>
        logger.error(s"'$entityName' with id '$id' not found.", iae)
        Future.failed(iae)
      case NonFatal(e) =>
        logger.error(s"'$entityName' with id '$id' not found.", e)
        Future.successful(None)
    }

    fromTry(eventualEntity).flatten
  }
}
