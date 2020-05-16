package dbd.api.database.access.many

import dbd.api.database.model.description.DeviceDescription
import dbd.api.database.model.device.ClientDevice
import dbd.core.model.existing
import dbd.core.model.enumeration.DescriptionRole.Name
import dbd.core.model.partial.{DescriptionData, DeviceDescriptionData}
import utopia.vault.database.Connection

/**
  * Used for accessing data of multiple devices
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object Devices
{
	private def factory = ClientDevice
	
	/**
	  * Inserts data for a new device
	  * @param deviceName Name of the device
	  * @param languageId Id of the language the name is written in
	  * @param authorId Id of the user who added this device
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted device description, containing new device id
	  */
	def insert(deviceName: String, languageId: Int, authorId: Int)(implicit connection: Connection) =
	{
		// Inserts a new device first
		val newDeviceId = factory.insert(authorId)
		// Then inserts a description for the device
		val dataToInsert = DeviceDescriptionData(newDeviceId, DescriptionData(Name, languageId, deviceName))
		val (linkId, description) = DeviceDescription.insert(dataToInsert)
		existing.DeviceDescription(linkId, dataToInsert.copy (description = description))
	}
}
