package dbd.api.database.access.many

import dbd.api.database.model.description.OrganizationDescription
import dbd.api.database.model.organization.{Organization, OrganizationMemberRole, OrganizationMembership}
import dbd.core.model.enumeration.DescriptionRole.Name
import dbd.core.model.enumeration.UserRole.Owner
import dbd.core.model.partial.{DescriptionData, MembershipData, OrganizationDescriptionData}
import utopia.vault.database.Connection

/**
  * Used for accessing multiple organizations at a time
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object Organizations
{
	// COMPUTED	----------------------
	
	private def factory = Organization
	
	
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
		val membership = OrganizationMembership.insert(MembershipData(organizationId, founderId, Some(founderId)))
		OrganizationMemberRole.insert(membership.id, Owner, founderId)
		// Inserts a name for that organization
		OrganizationDescription.insert(OrganizationDescriptionData(organizationId,
			DescriptionData(Name, languageId, organizationName, Some(founderId))))
		organizationId
	}
}
