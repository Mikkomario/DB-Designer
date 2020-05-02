package dbd.api.database.access.single

import dbd.api.database
import dbd.core.model.enumeration.DescriptionRole
import dbd.core.model.enumeration.DescriptionRole.Name
import dbd.core.model.existing
import dbd.core.model.partial.{DescriptionData, DeviceDescriptionData}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleModelAccess, UniqueAccess}

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
	}
}
