package dbd.api.database.factory.user

import dbd.api.database.Tables
import dbd.api.database.model.user.{UserDeviceModel, UserLanguageModel, UserSettingsModel}
import dbd.core.model.combined.user.UserWithLinks
import dbd.core.model.existing
import dbd.core.model.existing.user
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.nosql.factory.{Deprecatable, LinkedFactory}
import utopia.vault.sql.{Select, Where}

object UserFactory extends LinkedFactory[user.User, user.UserSettings] with Deprecatable
{
	// IMPLEMENTED	-----------------------------------
	
	override def nonDeprecatedCondition = UserSettingsModel.nonDeprecatedCondition
	
	override def table = Tables.user
	
	override def childFactory = UserSettingsModel
	
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
		val languageIds = connection(Select(UserLanguageModel.table, UserLanguageModel.languageIdAttName) +
			Where(UserLanguageModel.withUserId(user.id).toCondition)).rowIntValues
		// Reads device links
		val deviceIds = connection(Select(UserDeviceModel.table, UserDeviceModel.deviceIdAttName) +
			Where(UserDeviceModel.withUserId(user.id).toCondition && UserDeviceModel.nonDeprecatedCondition)).rowIntValues
		
		// Combines data
		UserWithLinks(user, languageIds, deviceIds)
	}
}


