package dbd.core.model.existing

import dbd.core.model.partial.UserSettingsData

/**
  * Represents stored user settings
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class UserSettings(id: Int, userId: Int, data: UserSettingsData) extends Stored[UserSettingsData]
