package dbd.api.database.model.description

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.existing
import dbd.core.model.existing.description
import dbd.core.model.existing.description.Description
import dbd.core.model.partial.description.{DescriptionData, DeviceDescriptionData}

import scala.util.Success

object DeviceDescriptionModel extends DescriptionLinkFactory[description.DeviceDescription, DeviceDescriptionModel,
	DeviceDescriptionData[DescriptionData]]
{
	// IMPLEMENTED	------------------------------
	
	override def targetIdAttName = "deviceId"
	
	override protected def apply(id: Int, targetId: Int, description: Description) =
		Success(existing.description.DeviceDescription(id, DeviceDescriptionData(targetId, description)))
	
	override def table = Tables.deviceDescription
}

/**
  * Used for interacting with links between devices and their descriptions
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class DeviceDescriptionModel(id: Option[Int] = None, deviceId: Option[Int] = None, descriptionId: Option[Int] = None,
								  deprecatedAfter: Option[Instant] = None)
	extends DescriptionLinkModel[description.DeviceDescription, DeviceDescriptionModel.type]
{
	override def targetId = deviceId
	
	override def factory = DeviceDescriptionModel
}
