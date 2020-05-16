package dbd.api.database.model.user

import java.time.Instant

import dbd.api.database.Tables
import dbd.api.model.existing
import dbd.api.model.partial.UserSessionData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.enumeration.ComparisonOperator.Larger
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, StorableFactoryWithValidation}

object SessionModel extends StorableFactoryWithValidation[existing.UserSession] with Deprecatable
{
	// IMPLEMENTED	-------------------------------
	
	// Non-deprecated keys must not be logged out or expired in the past
	override def nonDeprecatedCondition = table("logoutTime").isNull &&
		expiringIn(Instant.now()).toConditionWithOperator(Larger)
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.UserSession(model("id").getInt,
		UserSessionData(model("userId").getInt, model("deviceId").getInt, model("key").getString,
			model("expiresIn").getInstant))
	
	override def table = Tables.userSession
	
	
	// COMPUTED	-----------------------------------
	
	/**
	  * @return A new model that has just been marked as logged out
	  */
	def nowLoggedOut = apply(logoutTime = Some(Instant.now()))
	
	
	// OTHER	-----------------------------------
	
	/**
	  * @param userId Id of the targeted user
	  * @return A model with only user id set
	  */
	def withUserId(userId: Int) = apply(userId = Some(userId))
	
	/**
	  * @param deviceId Id of the targeted device
	  * @return A model with only device id set
	  */
	def withDeviceId(deviceId: Int) = apply(deviceId = Some(deviceId))
	
	/**
	  * @param key Session key
	  * @return A model with only key set
	  */
	def withKey(key: String) = apply(key = Some(key))
	
	/**
	  * @param expireTime Session key expiration timestamp
	  * @return A model with only expiration time set
	  */
	def expiringIn(expireTime: Instant) = apply(expires = Some(expireTime))
	
	/**
	  * Inserts a new user session to DB
	  * @param data Data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted session
	  */
	def insert(data: UserSessionData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.userId), Some(data.deviceId), Some(data.key), Some(data.expires)).insert().getInt
		existing.UserSession(newId, data)
	}
}

/**
  * Used for interacting with user session data in DB
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
case class SessionModel(id: Option[Int] = None, userId: Option[Int] = None, deviceId: Option[Int] = None,
						key: Option[String] = None, expires: Option[Instant] = None, logoutTime: Option[Instant] = None)
	extends StorableWithFactory[existing.UserSession]
{
	// IMPLEMENTED	-------------------------------
	
	override def factory = SessionModel
	
	override def valueProperties = Vector("id" -> id, "userId" -> userId, "deviceId" -> deviceId, "key" -> key,
		"expiresIn" -> expires, "logoutTime" -> logoutTime)
	
	
	// OTHER	------------------------------------
	
	/**
	  * @param deviceId Id of targeted device
	  * @return A copy of this model with specified device id
	  */
	def withDeviceId(deviceId: Int) = copy(deviceId = Some(deviceId))
}
