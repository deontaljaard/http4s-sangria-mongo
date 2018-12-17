package server

import model.device.Device
import model.person.{Person, RegistrationData}
import org.joda.time.DateTime
import sangria.ast.StringValue
import sangria.macros.derive._
import sangria.schema._
import sangria.validation.Violation
import service.ReactiveMongoRegistry

import scala.util.Try

object SchemaDefinition {

  val Id = Argument("id", StringType)

  case object DateTimeCoerceViolation extends Violation {
    override def errorMessage: String = "Error during parsing DateTime"
  }

  implicit val GraphQLDateTime = ScalarType[DateTime](//1
    "DateTime", //2
    coerceOutput = (dt, _) => dt.toString, //3
    coerceInput = { //4
      case StringValue(dt, _, _, _, _) => Try {
        Right(DateTime.parse(dt))
      }.getOrElse(Left(DateTimeCoerceViolation))
      case _ => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = { //5
      case s: String => Try {
        Right(DateTime.parse(s))
      }.getOrElse(Left(DateTimeCoerceViolation))
      case _ => Left(DateTimeCoerceViolation)
    }
  )

  implicit val RegistrationDataType =
    deriveObjectType[Unit, RegistrationData](
      ObjectTypeDescription("The registration data for the person")
    )

  implicit val PersonType =
    deriveObjectType[Unit, Person](
      ObjectTypeDescription("A person"),
      DocumentField("registrationData", "Metadata about the person's registration"))

  implicit val DeviceType =
    deriveObjectType[Unit, Device](
      ObjectTypeDescription("A device"))

  implicit val PersonT = ObjectType(
    "Person",
    "A registered person",

    fields[Unit, Person](
      Field("id", StringType, resolve = _.value.id),
      Field("firstName", StringType, resolve = _.value.firstName),
      Field("lastName", StringType, resolve = _.value.lastName),
      Field("email", StringType, resolve = _.value.email),
      Field("registrationData", OptionType(RegistrationDataType),
        description = Some("The registration data for the person"),
        resolve = _.value.registrationData),
      Field("created", OptionType(GraphQLDateTime),
        description = Some("The date the person was created"),
        resolve = _.value.created)))

  val LimitArg = Argument("limit", OptionInputType(IntType), defaultValue = 20)
  val OffsetArg = Argument("offset", OptionInputType(IntType), defaultValue = 0)

  val QueryType = ObjectType(
    "Query", fields[ReactiveMongoRegistry, Unit](
      Field("person", OptionType(PersonType),
        description = Some("Returns a person with specific `id`."),
        arguments = Id :: Nil,
        resolve = c => c.ctx.personService.findById(c.arg(Id))
      ),
      Field("device", OptionType(DeviceType),
        description = Some("Returns a device with specific `uid`."),
        arguments = Id :: Nil,
        resolve = c => c.ctx.deviceService.findByDeviceUID(c.arg(Id))
      ),
      Field("devices", ListType(DeviceType),
        description = Some("Returns a list of devices associated with a person `uid`."),
        arguments = Id :: Nil,
        resolve = c => c.ctx.deviceService.findByPersonId(c.arg(Id))
      )
    )
  )

  val schema = Schema(QueryType)
}
