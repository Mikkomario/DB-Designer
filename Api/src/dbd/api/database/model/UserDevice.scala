package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.factory.Deprecatable

object UserDevice extends Deprecatable
{
	// ATTRIBUTES	----------------------
	
	/**
	  * Name of the attribute containing the device id
	  */
	val deviceIdAttName = "deviceId"
	
	
	// COMPUTED	--------------------------
	
	def table = Tables.userDevice
	
	
	// IMPLEMENTED	----------------------
	
	override def nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	
	// OTHER	--------------------------
	
	/**
	  * @param userId Id of described user
	  * @return A model with only user id set
	  */
	def withUserId(userId: Int) = apply(userId = Some(userId))
	
	/**
	  * Inserts a new connection between a user and a client device
	  * @param userId Id of the user
	  * @param deviceId Id of the device
	  * @param connection DB Connection (implicit)
	  * @return Id of the newly created link
	  */
	def insert(userId: Int, deviceId: Int)(implicit connection: Connection) =
		apply(None, Some(userId), Some(deviceId)).insert().getInt
}

/**
  * Registers links between users and devices
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class UserDevice(id: Option[Int] = None, userId: Option[Int] = None, deviceId: Option[Int] = None,
					  deprecatedAfter: Option[Instant] = None) extends Storable
{
	override def table = UserDevice.table
	
	override def valueProperties = Vector("id" -> id, "userId" -> userId, "deviceId" -> deviceId,
		"deprecatedAfter" -> deprecatedAfter)
}