package model.person

import org.joda.time.DateTime

case class Person(id: String = "",
                  firstName: String,
                  lastName: String,
                  email: String,
                  registrationData: RegistrationData,
                  created: DateTime = DateTime.now)
