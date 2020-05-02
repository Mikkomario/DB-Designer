package dbd.core.model.existing

import dbd.core.model.template.Extender

/**
  * Represents a user registered in the database
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param id This user's id in DB
  * @param settings This user's current settings
  */
case class User(id: Int, settings: UserSettings) extends Extender[UserSettings]
{
	override def wrapped = settings
}
