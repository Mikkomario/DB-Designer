package dbd.api.database.access.single

import dbd.api.database
import dbd.api.model.existing
import utopia.vault.database.Connection
import utopia.vault.nosql.access.SingleModelAccess

/**
  * Used for accessing individual user sessions in DB
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
object UserSession extends SingleModelAccess[existing.UserSession]
{
	// IMPLEMENTED	-----------------------------
	
	override def factory = database.model.UserSession
	
	override def globalCondition = Some(factory.nonDeprecatedCondition)
	
	
	// OTHER	---------------------------------
	
	/**
	  * @param sessionKey A session key
	  * @param connection DB Connection (implicit)
	  * @return An active session matching specified session key
	  */
	def matching(sessionKey: String)(implicit connection: Connection) =
		find(factory.withKey(sessionKey).toCondition)
}
