package dbd.core.model.existing

import dbd.core.model.partial.DeviceDescriptionData
import dbd.core.model.template.DescriptionLinkLike

/**
  * Represents a device description stored in the database
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class DeviceDescription(id: Int, data: DeviceDescriptionData[Description])
	extends Stored[DeviceDescriptionData[Description]] with DescriptionLinkLike[Description]
{
	override def targetId = data.targetId
	
	override def description = data.description
}
