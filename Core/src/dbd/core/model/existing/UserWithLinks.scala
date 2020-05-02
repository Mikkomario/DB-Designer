package dbd.core.model.existing

import dbd.core.model.template.Extender

/**
  * This user model contains links to known languages and used devices
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param base Standard user data
  * @param languageIds Ids of the languages known to the user
  * @param deviceIds Ids of the devices known to the user
  */
case class UserWithLinks(base: User, languageIds: Vector[Int], deviceIds: Vector[Int]) extends Extender[User]
{
	override def wrapped = base
}
