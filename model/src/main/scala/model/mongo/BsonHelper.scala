package model.mongo

import org.joda.time.DateTime
import reactivemongo.bson.{BSONDateTime, BSONReader, BSONWriter}

object BsonHelper {
  implicit def dateTimeWriter: BSONWriter[DateTime, BSONDateTime] =
    (dt: DateTime) => BSONDateTime(dt.getMillis)

  implicit def dateTimeReader: BSONReader[BSONDateTime, DateTime] =
    (bson: BSONDateTime) => new DateTime(bson.as[BSONDateTime].value)
}
