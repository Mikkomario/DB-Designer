package dbd.api.database.model

import dbd.api.database.Tables
import dbd.core.model.combined.UserWithLinks
import dbd.core.model.{combined, existing}
import dbd.core.model.partial.UserSettingsData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.factory.{Deprecatable, LinkedStorableFactory}
import utopia.vault.sql.{Select, Where}

object User extends LinkedStorableFactory[existing.User, existing.UserSettings] with Deprecatable
{
	// IMPLEMENTED	-----------------------------------
	
	override def nonDeprecatedCondition = UserSettings.nonDeprecatedCondition
	
	override def table = Tables.user
	
	override def childFactory = UserSettings
	
	override def apply(model: Model[Constant], child: existing.UserSettings) =
		table.requirementDeclaration.validate(model).toTry.map { valid =>
			existing.User(valid("id").getInt, child)
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
		val newSettings = UserSettings.insert(userId, settings)
		UserAuth.insert(userId, password)
		existing.User(userId, newSettings)
	}
	
	/**
	  * Completes a normal user's data to include linked language and device ids
	  * @param user User to complete
	  * @param connection DB Connection
	  * @return User with associated data added
	  */
	def complete(user: existing.User)(implicit connection: Connection) =
	{
		// Reads language links
		val languageIds = connection(Select(UserLanguage.table, UserLanguage.languageIdAttName) +
			Where(UserLanguage.withUserId(user.id).toCondition)).rowIntValues
		// Reads device links
		val deviceIds = connection(Select(UserDevice.table, UserDevice.deviceIdAttName) +
			Where(UserDevice.withUserId(user.id).toCondition && UserDevice.nonDeprecatedCondition)).rowIntValues
		
		// Combines data
		combined.UserWithLinks(user, languageIds, deviceIds)
	}
}

/**
  * Used for interacting with user data in DB
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class User(id: Option[Int] = None) extends Storable
{
	override def table = User.table
	
	override def valueProperties = Vector("id" -> id)
}
