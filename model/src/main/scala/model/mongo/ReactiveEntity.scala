package model.mongo

import reactivemongo.bson.BSONObjectID

trait ReactiveEntity[A] {
  def _id: BSONObjectID
}

