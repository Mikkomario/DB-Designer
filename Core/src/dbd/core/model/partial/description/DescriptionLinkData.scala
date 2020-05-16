package dbd.core.model.partial.description

import dbd.core.model.existing.description.Description
import dbd.core.model.template.DescriptionLinkLike
import utopia.flow.generic.ModelConvertible

object DescriptionLinkData
{
	/**
	  * Description link data for descriptions that haven't been inserted to database yet
	  */
	type PartialDescriptionLinkData = DescriptionLinkData[DescriptionData]
	
	/**
	  * Description link data for stored descriptions
	  */
	type FullDescriptionLinkData = DescriptionLinkData[Description]
}

/**
  * Contains basic data for a description link
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param targetId Id of the described target
  * @param description Description of the device
  * @tparam D Type of description contained within this data
  */
case class DescriptionLinkData[+D <: ModelConvertible](targetId: Int, description: D) extends DescriptionLinkLike[D]
