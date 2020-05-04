package dbd.core.model.partial

import dbd.core.model.template.DescriptionLinkLike

/**
  * Contains basic data for a device-description
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param deviceId Id of the described device
  * @param description Description of the device
  * @tparam D Type of description contained within this data
  */
case class DeviceDescriptionData[+D](deviceId: Int, description: D) extends DescriptionLinkLike[D]
{
	override def targetId = deviceId
}
