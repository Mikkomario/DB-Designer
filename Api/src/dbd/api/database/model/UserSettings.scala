package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.partial.UserSettingsData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, StorableFactoryWithValidation}

object UserSettings extends StorableFactoryWithValidation[existing.UserSettings] with Deprecatable
{
	// ATTRIBUTES	----------------------------------
	
	/**
	  * Name of the attribute for user's id
	  */
	val userIdAttName = "userId"
	
	
	// IMPLEMENTED	----------------------------------
	
	override val nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override def table = Tables.userSettings
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.UserSettings(model("id").getInt,
		model(userIdAttName).getInt, UserSettingsData(model("name").getString, model("email").getString))
	
	
	// OTHER	--------------------------------------
	
	/**
	  * @param userId Id of the described user
	  * @return A model with only user id set
	  */
	def withUserId(userId: Int) = apply(userId = Some(userId))
	
	/**
	  * @param userName User name
	  * @return a model with only user name set
	  */
	def withName(userName: String) = apply(name = Some(userName))
	
	/**
	  * @param email Email
	  * @return A model with only email set
	  */
	def withEmail(email: String) = apply(email = Some(email))
	
	/**
	  * Inserts a new set of user settings to the DB (please deprecate old version first)
	  * @param userId Id of the described user
	  * @param data New user settings data
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted data
	  */
	def insert(userId: Int, data: UserSettingsData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(userId), Some(data.name), Some(data.email)).insert().getInt
		existing.UserSettings(newId, userId, data)
	}
}

/**
  * Used for interacting with user settings in DB
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class UserSettings(id: Option[Int] = None, userId: Option[Int] = None, name: Option[String] = None,
						email: Option[String] = None, deprecatedAfter: Option[Instant] = None)
	extends StorableWithFactory[existing.UserSettings]
{
	import UserSettings._
	
	override def factory = UserSettings
	
	override def valueProperties = Vector("id" -> id, userIdAttName -> userId, "name" -> name, "email" -> email,
		"deprecatedAfter" -> deprecatedAfter)
}
