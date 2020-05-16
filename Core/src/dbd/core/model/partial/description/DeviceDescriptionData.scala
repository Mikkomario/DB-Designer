package dbd.core.model.partial.description

import dbd.core.model.template.DescriptionLinkLike
import utopia.flow.generic.ModelConvertible

/**
  * Contains basic data for a device-description
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param deviceId Id of the described device
  * @param description Description of the device
  * @tparam D Type of description contained within this data
  */
@deprecated("Replaced with DescriptionLinkData", "v2")
case class DeviceDescriptionData[+D <: ModelConvertible](deviceId: Int, description: D) extends DescriptionLinkLike[D]
{
	override def targetId = deviceId
}
