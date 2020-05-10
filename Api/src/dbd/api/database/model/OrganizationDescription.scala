package dbd.api.database.model

import dbd.core.model.existing
import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.partial.{DescriptionData, OrganizationDescriptionData}

import scala.util.Success

object OrganizationDescription extends DescriptionLinkFactory[existing.OrganizationDescription,
	OrganizationDescription, OrganizationDescriptionData[DescriptionData]]
{
	// IMPLEMENTED	------------------------------
	
	override def targetIdAttName = "organizationId"
	
	override protected def apply(id: Int, targetId: Int, description: existing.Description) =
		Success(existing.OrganizationDescription(id, OrganizationDescriptionData(targetId, description)))
	
	override def table = Tables.organizationDescription
}

/**
  * Used for interacting with organization description links in DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class OrganizationDescription(id: Option[Int] = None, organizationId: Option[Int] = None,
								   descriptionId: Option[Int] = None, deprecatedAfter: Option[Instant] = None)
	extends DescriptionLink[existing.OrganizationDescription, OrganizationDescription.type]
{
	override def factory = OrganizationDescription
	
	override def targetId = organizationId
}
