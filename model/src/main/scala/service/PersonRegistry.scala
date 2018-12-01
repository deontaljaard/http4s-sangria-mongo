package service

import model.person.{Person, PersonRepositoryComponent}

import scala.concurrent.Future

trait PersonRegistry extends PersonRepositoryComponent with PersonServiceComponent

trait PersonService {
  def findById(personId: String): Future[Option[Person]]
}

trait PersonServiceComponent {
  this: PersonRepositoryComponent =>
  val personService: PersonService

  class PersonServiceImpl extends PersonService {
    override def findById(personId: String): Future[Option[Person]] =
      personRepository.findById(personId)
  }
}
