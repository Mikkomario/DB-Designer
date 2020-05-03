package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import dbd.api.model.existing
import dbd.api.model.partial.DeviceKeyData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, StorableFactoryWithValidation}

object DeviceKey extends StorableFactoryWithValidation[existing.DeviceKey] with Deprecatable
{
	// IMPLEMENTED	------------------------------
	
	override val nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.DeviceKey(model("id").getInt,
		DeviceKeyData(model("userId").getInt, model("deviceId").getInt, model("key").getString))
	
	override def table = Tables.deviceAuthKey
	
	
	// COMPUTED	----------------------------------
	
	/**
	  * @return A model that has just been marked as deprecated
	  */
	def nowDeprecated = apply(deprecatedAfter = Some(Instant.now()))
	
	
	// OTHER	----------------------------------
	
	/**
	  * @param userId Id of the linked user
	  * @return A model with only user id set
	  */
	def withUserId(userId: Int) = apply(userId = Some(userId))
	
	/**
	  * @param deviceId Id of targeted device
	  * @return A model with only device id set
	  */
	def withDeviceId(deviceId: Int) = apply(deviceId = Some(deviceId))
	
	/**
	  * @param key Authorization key
	  * @return A model with only key set
	  */
	def withKey(key: String) = apply(key = Some(key))
	
	/**
	  * Inserts a new device authentication key to the database
	  * @param data Data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted device key
	  */
	def insert(data: DeviceKeyData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.userId), Some(data.deviceId), Some(data.key)).insert().getInt
		existing.DeviceKey(newId, data)
	}
}

/**
  * Used for interacting with device authentication keys in DB
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
case class DeviceKey(id: Option[Int] = None, userId: Option[Int] = None, deviceId: Option[Int] = None,
					 key: Option[String] = None, deprecatedAfter: Option[Instant] = None)
	extends StorableWithFactory[existing.DeviceKey]
{
	override def factory = DeviceKey
	
	override def valueProperties = Vector("id" -> id, "userId" -> userId, "deviceId" -> deviceId, "key" -> key,
		"deprecatedAfter" -> deprecatedAfter)
}
