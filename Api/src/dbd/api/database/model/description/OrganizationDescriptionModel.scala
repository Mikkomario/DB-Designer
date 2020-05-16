package dbd.api.database.model.description

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.existing
import dbd.core.model.existing.description
import dbd.core.model.existing.description.Description
import dbd.core.model.partial.description.{DescriptionData, OrganizationDescriptionData}

import scala.util.Success

object OrganizationDescriptionModel extends DescriptionLinkFactory[description.OrganizationDescription,
	OrganizationDescriptionModel, OrganizationDescriptionData[DescriptionData]]
{
	// IMPLEMENTED	------------------------------
	
	override def targetIdAttName = "organizationId"
	
	override protected def apply(id: Int, targetId: Int, description: Description) =
		Success(existing.description.OrganizationDescription(id, OrganizationDescriptionData(targetId, description)))
	
	override def table = Tables.organizationDescription
}

/**
  * Used for interacting with organization description links in DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class OrganizationDescriptionModel(id: Option[Int] = None, organizationId: Option[Int] = None,
										descriptionId: Option[Int] = None, deprecatedAfter: Option[Instant] = None)
	extends DescriptionLinkModel[description.OrganizationDescription, OrganizationDescriptionModel.type]
{
	override def factory = OrganizationDescriptionModel
	
	override def targetId = organizationId
}
