package dbd.api.database.access.single

import dbd.api.database
import dbd.api.model.existing
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleIdModelAccess, SingleModelAccess}

/**
  * Used for accessing individual device keys in DB
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
object DeviceKey extends SingleModelAccess[existing.DeviceKey]
{
	// IMPLEMENTED	------------------------------------
	
	override def factory = database.model.DeviceKey
	
	override def globalCondition = Some(factory.nonDeprecatedCondition)
	
	
	// OTHER	----------------------------------------
	
	/**
	  * @param id Device key id
	  * @return An access point to a device key by that id
	  */
	def apply(id: Int) = new SingleDeviceKeyById(id)
	
	/**
	  * @param key Authorization key
	  * @param connection DB Connection (implicit)
	  * @return A device key that matches specified authorization key
	  */
	def matching(key: String)(implicit connection: Connection) = find(factory.withKey(key).toCondition)
	
	
	// NESTED	-----------------------------------------
	
	class SingleDeviceKeyById(deviceKeyId: Int) extends SingleIdModelAccess[existing.DeviceKey](deviceKeyId, DeviceKey.factory)
	{
		/**
		  * Invalidates this key so that it can no longer be used for authenticating requests
		  * @param connection DB Connection (implicit)
		  * @return Whether any change was made
		  */
		def invalidate()(implicit connection: Connection) = DeviceKey.factory.nowDeprecated.updateWhere(
			condition && DeviceKey.factory.nonDeprecatedCondition) > 0
	}
}