package dbd.core.model.combined

import dbd.core.model.existing.User
import dbd.core.model.template.Extender
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.datastructure.template.{Model, Property}
import utopia.flow.generic.ValueConversions._
import utopia.flow.generic.{FromModelFactory, ModelConvertible}

object UserWithLinks extends FromModelFactory[UserWithLinks]
{
	override def apply(model: Model[Property]) = User(model).map { user =>
		val languageIds = model("language_ids").getVector.flatMap { _.int }
		val deviceIds = model("device_ids").getVector.flatMap { _.int }
		
		UserWithLinks(user, languageIds, deviceIds)
	}
}

/**
  * This user model contains links to known languages and used devices
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param base Standard user data
  * @param languageIds Ids of the languages known to the user
  * @param deviceIds Ids of the devices known to the user
  */
case class UserWithLinks(base: User, languageIds: Vector[Int], deviceIds: Vector[Int]) extends Extender[User] with ModelConvertible
{
	override def wrapped = base
	
	override def toModel =
	{
		// Adds additional data to the standard model
		base.toModel + Constant("language_ids", languageIds) + Constant("device_ids", deviceIds)
	}
}
