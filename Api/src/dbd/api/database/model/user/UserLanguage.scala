package dbd.api.database.model.user

import dbd.api.database.Tables
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable

object UserLanguage
{
	// ATTRIBUTES	-------------------------
	
	/**
	  * Name of the attribute that contains linked language's id
	  */
	val languageIdAttName = "languageId"
	
	
	// COMPUTED	-----------------------------
	
	def table = Tables.userLanguage
	
	
	// OTHER	-----------------------------
	
	/**
	  * @param userId Id of the described user
	  * @return Model with only user id set
	  */
	def withUserId(userId: Int) = apply(userId = Some(userId))
	
	/**
	  * Inserts a new connection between a user and a language
	  * @param userId Id of the user
	  * @param languageId Id of the language
	  * @return Id of the newly inserted link
	  */
	def insert(userId: Int, languageId: Int)(implicit connection: Connection) =
		apply(None, Some(userId), Some(languageId)).insert().getInt
}

/**
  * Used for interacting with user-language-links in DB
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class UserLanguage(id: Option[Int] = None, userId: Option[Int] = None, languageId: Option[Int] = None) extends Storable
{
	import UserLanguage._
	
	override def table = UserLanguage.table
	
	override def valueProperties = Vector("id" -> id, "userId" -> userId, languageIdAttName -> languageId)
}
