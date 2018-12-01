package model.person

import com.typesafe.scalalogging.LazyLogging
import model.config.Http4sSangriaAppConfig
import model.device.Device
import model.mongo.{ReactiveClientBuilder, ReactiveEntity}
import model.mongo.ReactiveQueryHelper._
import org.joda.time.DateTime
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson._

import scala.concurrent.Future

trait PersonRepository {
  def findById(personId: String): Future[Option[Person]]
}

trait PersonRepositoryComponent {
  val personRepository: PersonRepository

  import scala.concurrent.ExecutionContext.Implicits.global
  import model.common.ExecutionContexts.futureToTask

  object ReactivePersonRepository {

    case class ReactivePerson(_id: BSONObjectID,
                              firstName: String,
                              lastName: String,
                              email: String,
                              registrationData: RegistrationData,
                              created: DateTime) extends ReactiveEntity[ReactivePerson]

    object ReactivePerson extends LazyLogging {
      private def mapToReactivePerson(objectId: BSONObjectID, person: Person): ReactivePerson =
        ReactivePerson(
          _id = objectId,
          firstName = person.firstName,
          lastName = person.lastName,
          email = person.email,
          registrationData = person.registrationData,
          created = person.created
        )

      def fromPerson(person: Person): ReactivePerson =
        mapToReactivePerson(BSONObjectID.generate(), person)

      def fromPersonWithId(person: Person): ReactivePerson =
        mapToReactivePerson(BSONObjectID.parse(person.id).get, person)

      def toPerson(reactivePerson: ReactivePerson): Person = Person(
        id = reactivePerson._id.stringify,
        firstName = reactivePerson.firstName,
        lastName = reactivePerson.lastName,
        email = reactivePerson.email,
        registrationData = reactivePerson.registrationData,
        created = reactivePerson.created
      )
    }

  }

  object ReactivePersonRepositoryBsonHelper {

    import ReactivePersonRepository._
    import model.mongo.BsonHelper._

    implicit def registrationDataReader: BSONDocumentReader[RegistrationData] = Macros.reader[RegistrationData]

    implicit def registrationDataWriter: BSONDocumentWriter[RegistrationData] = Macros.writer[RegistrationData]

    implicit def personReader: BSONDocumentReader[ReactivePerson] = Macros.reader[ReactivePerson]

    implicit def personWriter: BSONDocumentWriter[ReactivePerson] = Macros.writer[ReactivePerson]

  }

  class ReactivePersonRepository extends PersonRepository with LazyLogging {

    import ReactivePersonRepository.ReactivePerson._
    import ReactivePersonRepository._
    import ReactivePersonRepositoryBsonHelper._

    private lazy val emailIdx = Index(key = Seq(("email", Ascending)), name = Some("email_uk_idx"), unique = true)

    private lazy val personsDb = Http4sSangriaAppConfig.DbConfig.personsDb
    private lazy val personsCollectionName = Http4sSangriaAppConfig.DbConfig.personsCollection
    private val personsCollection: Future[BSONCollection] = for {
      collection <- ReactiveClientBuilder.collectionFromDataBase(personsDb, personsCollectionName)
      writeResult <- collection.indexesManager.ensure(emailIdx)
      _ = logger.info(s"Person email index write result '$writeResult'")
    } yield collection

    override def findById(personId: String): Future[Option[Person]] =
      findEntityById[ReactivePerson, Person](personId, personsCollection)(toPerson)
  }
}
