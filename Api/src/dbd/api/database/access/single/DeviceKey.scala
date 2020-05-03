package dbd.api.database.access.single

import dbd.api.database
import dbd.api.model.existing
import utopia.vault.database.Connection
import utopia.vault.nosql.access.SingleModelAccess

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
	  * @param key Authorization key
	  * @param connection DB Connection (implicit)
	  * @return A device key that matches specified authorization key
	  */
	def matching(key: String)(implicit connection: Connection) = find(factory.withKey(key).toCondition)
}
