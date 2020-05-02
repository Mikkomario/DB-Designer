package dbd.api.database.model

import dbd.api.database.Tables
import dbd.core.model.existing
import dbd.core.model.partial.UserSettingsData
import utopia.flow.datastructure.template.{Model, Property}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.{Result, Storable, StorableWithFactory}
import utopia.vault.nosql.factory.{Deprecatable, FromResultFactory, StorableFactory}
import utopia.vault.util.ErrorHandling

import scala.util.{Failure, Success}

object User extends FromResultFactory[existing.User] with Deprecatable
{
	// IMPLEMENTED	-----------------------------------
	
	override def nonDeprecatedCondition = UserSettings.nonDeprecatedCondition
	
	override def table = Tables.user
	
	override def joinedTables = UserSettings.tables :+ UserLanguage.table
	
	override def apply(result: Result) =
	{
		// Groups rows by user id and parses each user
		result.grouped(table, UserLanguage.table).flatMap { case (userId, userData) =>
			val (userRow, languageLinkRows) = userData
			// Parses current settings
			UserSettings(userRow).map { settings =>
				// Parses language links
				val languageIds = languageLinkRows.flatMap { _(UserLanguage.languageIdAttName).int }
				existing.User(userId.getInt, settings, languageIds)
			} match
			{
				case Success(user) => Some(user)
				case Failure(error) =>
					ErrorHandling.modelParsePrinciple.handle(error)
					None
			}
		}.toVector
	}
	
	
	// OTHER	-------------------------------------
	
	/**
	  * Inserts a new user to the database
	  * @param settings User settings
	  * @param languageIds Ids of languages known by the user
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted user
	  */
	def insert(settings: UserSettingsData, languageIds: Vector[Int])(implicit connection: Connection) =
	{
		// Inserts the user first, then links new data
		val userId = apply().insert().getInt
		val newSettings = UserSettings.insert(userId, settings)
		languageIds.foreach { languageId => UserLanguage.insert(userId, languageId) }
		existing.User(userId, newSettings, languageIds)
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
