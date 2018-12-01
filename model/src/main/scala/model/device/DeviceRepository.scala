package model.device

import com.typesafe.scalalogging.LazyLogging
import model.config.Http4sSangriaAppConfig
import model.mongo.ReactiveQueryHelper._
import model.mongo.{ReactiveClientBuilder, ReactiveEntity}
import org.joda.time.DateTime
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.bson._

import scala.concurrent.Future

trait DeviceRepository {
  def findByDeviceUID(deviceUID: String): Future[Option[Device]]

  def findByPersonId(personId: String, pageSize: Int = 50, pageNum: Int = 1): Future[List[Device]]
}

trait DeviceRepositoryComponent {
  val deviceRepository: DeviceRepository

  import scala.concurrent.ExecutionContext.Implicits.global

  object ReactiveDeviceRepository {

    case class ReactiveDevice(_id: BSONObjectID,
                              deviceUID: String,
                              personId: BSONObjectID,
                              created: DateTime) extends ReactiveEntity[ReactiveDevice]

    object ReactiveDevice extends LazyLogging {
      private def mapToReactiveDevice(objectId: BSONObjectID, device: Device): ReactiveDevice =
        ReactiveDevice(
          _id = objectId,
          deviceUID = device.deviceUID,
          personId = BSONObjectID.parse(device.personId).get,
          created = device.created
        )

      def fromDevice(device: Device): ReactiveDevice =
        mapToReactiveDevice(BSONObjectID.generate(), device)

      def fromDeviceWithId(device: Device): ReactiveDevice =
        mapToReactiveDevice(BSONObjectID.parse(device.id).get, device)

      def toDevice(reactiveDevice: ReactiveDevice): Device = Device(
        id = reactiveDevice._id.stringify,
        deviceUID = reactiveDevice.deviceUID,
        personId = reactiveDevice.personId.stringify,
        created = reactiveDevice.created
      )
    }

  }

  object ReactiveDeviceRepositoryBsonHelper {

    import ReactiveDeviceRepository._
    import model.mongo.BsonHelper._

    implicit def deviceReader: BSONDocumentReader[ReactiveDevice] = Macros.reader[ReactiveDevice]

    implicit def deviceWriter: BSONDocumentWriter[ReactiveDevice] = Macros.writer[ReactiveDevice]

  }

  class ReactiveDeviceRepository extends DeviceRepository with LazyLogging {

    import ReactiveDeviceRepository.ReactiveDevice._
    import ReactiveDeviceRepository._
    import ReactiveDeviceRepositoryBsonHelper._

    private lazy val deviceUIDIdx = Index(key = Seq(("deviceUID", Ascending)), name = Some("device_uid_idx"), unique = true)

    private lazy val personsDb = Http4sSangriaAppConfig.DbConfig.personsDb
    private lazy val devicesCollectionName = Http4sSangriaAppConfig.DbConfig.devicesCollection
    private val devicesCollection: Future[BSONCollection] = for {
      collection <- ReactiveClientBuilder.collectionFromDataBase(personsDb, devicesCollectionName)
      writeResult <- collection.indexesManager.ensure(deviceUIDIdx)
      _ = logger.info(s"Device UID index write result '$writeResult'")
    } yield collection

    private val byDeviceUID: String => BSONDocument = deviceUID => byStringFields(Seq("deviceUID" -> deviceUID))
    val byPersonId: BSONObjectID => BSONDocument = objectId => document("personId" -> objectId)

    override def findByDeviceUID(deviceUID: String): Future[Option[Device]] = {
      logger.info(s"Looking for device by device UID '$deviceUID'.")

      val eventualMaybeDevice = for {
        collection <- devicesCollection
        maybeDevice <- collection.find(byDeviceUID(deviceUID)).one[ReactiveDevice]
      } yield maybeDevice.map(toDevice)

      eventualMaybeDevice
    }

    override def findByPersonId(personId: String, pageSize: Int, pageNum: Int): Future[List[Device]] =
      for {
        collection <- devicesCollection
        reactiveDevices <- collection.find(byPersonId(BSONObjectID.parse(personId).get))
          .options(QueryOpts(skipN = pageSize * (pageNum - 1)))
          .cursor[ReactiveDevice]()
          .collect[List](pageSize, Cursor.FailOnError[List[ReactiveDevice]]())
      } yield reactiveDevices.map(toDevice)
  }

}
