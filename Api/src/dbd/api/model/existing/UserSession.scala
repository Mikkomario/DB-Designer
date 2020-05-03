package dbd.api.model.existing

import dbd.api.model.partial.UserSessionData
import dbd.core.model.existing.Stored

/**
  * Represents a user session that has been stored to the DB
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
case class UserSession(id: Int, data: UserSessionData) extends Stored[UserSessionData]
