package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.existing
import dbd.core.model.partial.{DescriptionData, DeviceDescriptionData}

import scala.util.Success

object DeviceDescription extends DescriptionLinkFactory[existing.DeviceDescription, DeviceDescription,
	DeviceDescriptionData[DescriptionData]]
{
	// IMPLEMENTED	------------------------------
	
	override def targetIdAttName = "deviceId"
	
	override protected def apply(id: Int, targetId: Int, description: existing.Description) =
		Success(existing.DeviceDescription(id, DeviceDescriptionData(targetId, description)))
	
	override def table = Tables.deviceDescription
}

/**
  * Used for interacting with links between devices and their descriptions
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class DeviceDescription(id: Option[Int] = None, deviceId: Option[Int] = None, descriptionId: Option[Int] = None,
							 deprecatedAfter: Option[Instant] = None)
	extends DescriptionLink[existing.DeviceDescription, DeviceDescription.type]
{
	override def targetId = deviceId
	
	override def factory = DeviceDescription
}
