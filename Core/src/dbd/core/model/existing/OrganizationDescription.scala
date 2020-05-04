package dbd.core.model.existing

import dbd.core.model.partial.OrganizationDescriptionData
import dbd.core.model.template.DescriptionLinkLike

/**
  * Represents a stored link between an organization and a description
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class OrganizationDescription(id: Int, data: OrganizationDescriptionData[Description])
	extends Stored[OrganizationDescriptionData[Description]] with DescriptionLinkLike[Description]
{
	override def targetId = data.targetId
	
	override def description = data.description
}
