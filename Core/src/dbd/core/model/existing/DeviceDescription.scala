package dbd.core.model.existing

import dbd.core.model.partial.DeviceDescriptionData

/**
  * Represents a device description stored in the database
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class DeviceDescription(id: Int, data: DeviceDescriptionData[Description])
	extends StoredDescriptionLink[DeviceDescriptionData[Description]]
