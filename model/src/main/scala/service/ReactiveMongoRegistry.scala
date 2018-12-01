package service

import model.device.DeviceRepository
import model.person.PersonRepository

trait ReactiveRegistry extends PersonRegistry with DeviceRegistry

class ReactiveMongoRegistry extends ReactiveRegistry {
  override val personService: PersonService = new PersonServiceImpl
  override val personRepository: PersonRepository = new ReactivePersonRepository
  override val deviceService: DeviceService = new DeviceServiceImpl
  override val deviceRepository: DeviceRepository = new ReactiveDeviceRepository
}
