package dbd.api.database.access.id

import dbd.api.database.model.UserSettings
import utopia.vault.database.Connection
import utopia.vault.sql.{Select, Where}

/**
  * Used for accessing individual user ids
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object UserId
{
	/**
	  * @param userName User name
	  * @param connection DB Connection (implicit)
	  * @return User id matching specified user name
	  */
	def forName(userName: String)(implicit connection: Connection) =
		userIdFromSettings(UserSettings.withName(userName))
	
	/**
	  * @param email User email address
	  * @param connection DB Connection (implicit)
	  * @return User id matching specified user email address
	  */
	def forEmail(email: String)(implicit connection: Connection) =
		userIdFromSettings(UserSettings.withEmail(email))
	
	private def userIdFromSettings(searchModel: UserSettings)(implicit connection: Connection) =
	{
		connection(Select(UserSettings.table, UserSettings.userIdAttName) +
			Where(searchModel.toCondition && UserSettings.nonDeprecatedCondition)).firstValue.int
	}
}
