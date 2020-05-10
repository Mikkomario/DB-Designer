package dbd.api.database.access.single

import dbd.api
import dbd.api.database
import dbd.api.model.partial.DeviceKeyData
import dbd.core.model.enumeration.DescriptionRole
import dbd.core.model.enumeration.DescriptionRole.Name
import dbd.core.model.existing
import dbd.core.model.partial.{DescriptionData, DeviceDescriptionData}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleModelAccess, UniqueAccess}
import java.util.UUID.randomUUID

import dbd.api.database.access.many.Descriptions

/**
  * Used for accessing and modifying individual devices
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object Device
{
	// COMPUTED	---------------------------------
	
	private def factory = database.model.ClientDevice
	
	
	// OTHER	---------------------------------
	
	/**
	  * @param id A device id
	  * @return An access point to that device's data
	  */
	def apply(id: Int) = new SingleDevice(id)
	
	
	// NESTED	---------------------------------
	
	class SingleDevice(deviceId: Int)
	{
		// COMPUTED	-----------------------------
		
		/**
		  * @param connection DB Connection (implicit)
		  * @return Whether a device with this id exists in the database
		  */
		def isDefined(implicit connection: Connection) = factory.table.containsIndex(deviceId)
		
		/**
		  * @return An access point to this device's authentication key
		  */
		def authenticationKey = DeviceAuthKey
		
		/**
		  * @return An access point to descriptions of this device
		  */
		def descriptions = Descriptions.ofDeviceWithId(deviceId)
		
		
		// OTHER	-----------------------------
		
		def nameInLanguageWithId(languageId: Int) = new DeviceDescription(Name, languageId)
		
		
		// NESTED	-----------------------------
		
		class DeviceDescription(descriptionType: DescriptionRole, languageId: Int)
			extends UniqueAccess[existing.DeviceDescription] with SingleModelAccess[existing.DeviceDescription]
		{
			// COMPUTED	-------------------------
			
			private def descriptionFactory = database.model.Description
			
			
			// IMPLEMENTED	---------------------
			
			override val condition = factory.nonDeprecatedCondition &&
				descriptionFactory.withRole(descriptionType).withLanguageId(languageId).toCondition
			
			override def factory = database.model.DeviceDescription
			
			
			// OTHER	------------------------
			
			/**
			  * Updates this device description
			  * @param newDescription New description for this device
			  * @param authorId Id of the user who wrote the description (optional)
			  * @param connection DB Connection (implicit)
			  * @return Newly inserted description
			  */
			def update(newDescription: String, authorId: Option[Int] = None)(implicit connection: Connection) =
			{
				// First deprecates the old description (link)
				factory.nowDeprecated.updateWhere(condition)
				// Inserts a new description
				factory.insert(DeviceDescriptionData(deviceId, DescriptionData(descriptionType, languageId,
					newDescription, authorId)))
			}
		}
		
		object DeviceAuthKey extends UniqueAccess[api.model.existing.DeviceKey] with SingleModelAccess[api.model.existing.DeviceKey]
		{
			// IMPLEMENTED	-----------------------
			
			override def condition = factory.withDeviceId(deviceId).toCondition && factory.nonDeprecatedCondition
			
			override def factory = database.model.DeviceKey
			
			
			// OTHER	---------------------------
			
			/**
			  * Updates this device authentication key
			  * @param userId The user that owns this new key
			  * @param key Key assigned to the user
			  * @param connection DB Connection (implicit)
			  * @return Newly inserted authentication key
			  */
			def update(userId: Int, key: String)(implicit connection: Connection) =
			{
				// Deprecates the old key
				factory.nowDeprecated.updateWhere(condition)
				// Inserts a new key
				factory.insert(DeviceKeyData(userId, deviceId, key))
			}
			
			/**
			  * Assings this device key to the specified user, invalidating previous users' keys
			  * @param userId If of the user receiving this device key
			  * @param connection DB Connection (implicit)
			  * @return This device key, now belonging to the specified user
			  */
			def assignToUserWithId(userId: Int)(implicit connection: Connection) = update(userId,
				randomUUID().toString)
			
			/**
			  * Releases this device authentication key from the specified user, if that user is currently holding this key
			  * @param userId Id of the user this key is released from
			  * @param connection DB Connection (implicit)
			  * @return Whether the user was holding this key (= whether any change was made)
			  */
			def releaseFromUserWithId(userId: Int)(implicit connection: Connection) =
			{
				// Deprecates the device key row (if not already deprecated)
				factory.nowDeprecated.updateWhere(mergeCondition(factory.withUserId(userId).toCondition)) > 0
			}
		}
	}
}
