package dbd.api.database.access.single.user

import java.time.Instant
import java.util.UUID.randomUUID

import dbd.api.database.access.many.user.DbUserSessions
import dbd.api.database.model.user
import dbd.api.model.existing
import dbd.api.model.partial.UserSessionData
import utopia.flow.util.TimeExtensions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleModelAccess, UniqueAccess}

/**
  * Used for accessing individual user sessions in DB
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
object DbUserSession extends SingleModelAccess[existing.UserSession]
{
	// IMPLEMENTED	-----------------------------
	
	override def factory = user.SessionModel
	
	override def globalCondition = Some(factory.nonDeprecatedCondition)
	
	
	// OTHER	---------------------------------
	
	/**
	  * @param userId Id of targeted user
	  * @param deviceId Id of targeted device
	  * @return An access point to the user's session on the specified device
	  */
	def apply(userId: Int, deviceId: Int) = new SingleDeviceSession(userId, deviceId)
	
	/**
	  * @param sessionKey A session key
	  * @param connection DB Connection (implicit)
	  * @return An active session matching specified session key
	  */
	def matching(sessionKey: String)(implicit connection: Connection) =
		find(factory.withKey(sessionKey).toCondition)
	
	
	// NESTED	----------------------------------
	
	class SingleDeviceSession(userId: Int, deviceId: Int) extends UniqueAccess[existing.UserSession]
		with SingleModelAccess[existing.UserSession]
	{
		// ATTRIBUTES	---------------------------
		
		private val targetingCondition = factory.withUserId(userId).withDeviceId(deviceId).toCondition
		
		
		// IMPLEMENTED	---------------------------
		
		override def condition = DbUserSession.mergeCondition(targetingCondition)
		
		override def factory = DbUserSession.factory
		
		
		// OTHER	-------------------------------
		
		/**
		  * Ends this user session (= logs the user out from this device)
		  * @param connection DB Connection (implicit)
		  * @return Whether any change was made
		  */
		def end()(implicit connection: Connection) =
		{
			// Deprecates existing active session
			factory.nowLoggedOut.updateWhere(condition) > 0
		}
		
		/**
		  * Starts a new session on this device. Logs out any previous user(s) of this device as well.
		  * @param connection DB Connection (implicit)
		  * @return New user session
		  */
		def start()(implicit connection: Connection) =
		{
			// Before starting a new session, makes sure to terminate existing user sessions for this device
			DbUserSessions.forDeviceWithId(deviceId).end()
			// Creates a new session that lasts for 24 hours or until logged out
			factory.insert(UserSessionData(userId, deviceId, randomUUID().toString, Instant.now() + 24.hours))
		}
	}
}
