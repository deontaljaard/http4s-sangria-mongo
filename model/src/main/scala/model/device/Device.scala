package model.device

import org.joda.time.DateTime

case class Device(id: String = "",
                  deviceUID: String,
                  personId: String,
                  created: DateTime = DateTime.now)
