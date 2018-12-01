package service

import model.device.{Device, DeviceRepositoryComponent}

import scala.concurrent.Future

trait DeviceRegistry extends DeviceRepositoryComponent with DeviceServiceComponent

trait DeviceService {
  def findByDeviceUID(deviceUID: String): Future[Option[Device]]

  def findByPersonId(personId: String): Future[List[Device]]
}

trait DeviceServiceComponent {
  this: DeviceRepositoryComponent =>
  val deviceService: DeviceService

  class DeviceServiceImpl extends DeviceService {
    override def findByDeviceUID(deviceUID: String): Future[Option[Device]] =
      deviceRepository.findByDeviceUID(deviceUID)

    override def findByPersonId(personId: String): Future[List[Device]] =
      deviceRepository.findByPersonId(personId)
  }

}
