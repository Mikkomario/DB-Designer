package dbd.api.database.model.user

import dbd.api.database.Tables
import dbd.core.model.combined.user.UserWithLinks
import dbd.core.model.existing.user
import dbd.core.model.partial.user.UserSettingsData
import dbd.core.model.existing
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.factory.{Deprecatable, LinkedStorableFactory}
import utopia.vault.sql.{Select, Where}

object UserModel extends LinkedStorableFactory[user.User, user.UserSettings] with Deprecatable
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
	  * Inserts a new user to the database
	  * @param settings User settings
	  * @param password Password for this user (not hashed yet)
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted user
	  */
	def insert(settings: UserSettingsData, password: String)(implicit connection: Connection) =
	{
		// Inserts the user first, then links new data
		val userId = apply().insert().getInt
		val newSettings = UserSettingsModel.insert(userId, settings)
		UserAuthModel.insert(userId, password)
		existing.user.User(userId, newSettings)
	}
	
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

/**
  * Used for interacting with user data in DB
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class UserModel(id: Option[Int] = None) extends Storable
{
	override def table = UserModel.table
	
	override def valueProperties = Vector("id" -> id)
}
