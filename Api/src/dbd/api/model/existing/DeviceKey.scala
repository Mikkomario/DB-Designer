package dbd.api.model.existing

import dbd.api.model.partial.DeviceKeyData
import dbd.core.model.existing.Stored

/**
  * Represents a device key that has been stored to DB
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
case class DeviceKey(id: Int, data: DeviceKeyData) extends Stored[DeviceKeyData]
