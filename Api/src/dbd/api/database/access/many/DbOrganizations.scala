package dbd.api.database.access.many

import dbd.api.database.model.description.OrganizationDescriptionModel
import dbd.api.database.model.organization.{OrganizationModel, MemberRoleModel, MembershipModel}
import dbd.core.model.enumeration.DescriptionRole.Name
import dbd.core.model.enumeration.UserRole.Owner
import dbd.core.model.partial.description.{DescriptionData, OrganizationDescriptionData}
import dbd.core.model.partial.organization.MembershipData
import utopia.vault.database.Connection

/**
  * Used for accessing multiple organizations at a time
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object DbOrganizations
{
	// COMPUTED	----------------------
	
	private def factory = OrganizationModel
	
	
	// OTHER	-----------------------
	
	/**
	  * Inserts a new organization to the database
	  * @param founderId Id of the user who created the organization
	  * @param connection DB Connection (implicit)
	  * @return Id of the newly inserted organization
	  */
	def insert(organizationName: String, languageId: Int, founderId: Int)(implicit connection: Connection) =
	{
		// Inserts a new organization
		val organizationId = factory.insert(founderId)
		// Adds the user to the organization (as owner)
		val membership = MembershipModel.insert(MembershipData(organizationId, founderId, Some(founderId)))
		MemberRoleModel.insert(membership.id, Owner, founderId)
		// Inserts a name for that organization
		OrganizationDescriptionModel.insert(OrganizationDescriptionData(organizationId,
			DescriptionData(Name, languageId, organizationName, Some(founderId))))
		organizationId
	}
}
