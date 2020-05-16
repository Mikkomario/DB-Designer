package dbd.core.model.existing.description

import dbd.core.model.partial.description.DeviceDescriptionData

/**
  * Represents a device description stored in the database
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
@deprecated("Replaced with DescriptionLink", "v2")
case class DeviceDescription(id: Int, data: DeviceDescriptionData[Description])
	extends StoredDescriptionLink[DeviceDescriptionData[Description]]
