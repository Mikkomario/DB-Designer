package dbd.api.database.factory.user

import dbd.api.database.Tables
import dbd.api.database.access.single.DbUser
import dbd.core.model.combined.user.UserWithLinks
import dbd.core.model.existing
import dbd.core.model.existing.user
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.nosql.factory.LinkedFactory

object UserFactory extends LinkedFactory[user.User, user.UserSettings]
{
	// IMPLEMENTED	-----------------------------------
	
	override def table = Tables.user
	
	override def childFactory = UserSettingsFactory
	
	override def apply(model: Model[Constant], child: user.UserSettings) =
		table.requirementDeclaration.validate(model).toTry.map { valid =>
			user.User(valid("id").getInt, child)
		}
	
	
	// OTHER	-------------------------------------
	
	/**
	  * Completes a normal user's data to include linked language and device ids
	  * @param user User to complete
	  * @param connection DB Connection
	  * @return User with associated data added
	  */
	def complete(user: existing.user.User)(implicit connection: Connection) =
	{
		// Reads language links
		val languages = DbUser(user.id).languages.all
		// Reads device links
		val deviceIds = DbUser(user.id).deviceIds
		
		// Combines data
		UserWithLinks(user, languages, deviceIds)
	}
}


