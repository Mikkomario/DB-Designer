package dbd.core.model.partial

import dbd.core.model.template.DescriptionLinkLike

/**
  * Basic data about a link between an organization and a description
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class OrganizationDescriptionData[+D](organizationId: Int, description: D) extends DescriptionLinkLike[D]
{
	override def targetId = organizationId
}
