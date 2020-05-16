package dbd.api.rest.resource.organization

import dbd.api.database.access.single.DbOrganization
import dbd.api.rest.resource.ResourceWithChildren
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.enumeration.TaskType.DeleteOrganization
import dbd.core.model.enumeration.UserRole.Owner
import utopia.access.http.Method.Delete
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.TimeExtensions._
import utopia.nexus.http.Path
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * A rest resource for accessing individual organization's data
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class OrganizationNode(organizationId: Int) extends ResourceWithChildren[AuthorizedContext]
{
	override def name = organizationId.toString
	
	// At this time, no methods are allowed for organization
	override val allowedMethods = Vector(Delete)
	
	override def children = Vector(
		OrganizationInvitationsNode(organizationId),
		OrganizationDescriptionsNode(organizationId),
		OrganizationMembersNode(organizationId),
		OrganizationDeletionsNode(organizationId)
	)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		// Makes sure the user is authorized to delete this organization
		context.authorizedForTask(organizationId, DeleteOrganization) { (session, membershipId, connection) =>
			implicit val c: Connection = connection
			// Checks whether there already exists a pending deletion
			val organization = DbOrganization(organizationId)
			val existingDeletions = organization.deletions.pending.all
			val deletion =
			{
				if (existingDeletions.nonEmpty)
					existingDeletions.head.deletion
				else
				{
					// Calculates the deletion period (how long this action can be cancelled) based on the number of
					// organization owners and users
					val numberOfOwners = organization.memberships.withRole(Owner).size
					val organizationSize = organization.memberships.size
					// Owners (other than requester) delay deletion by a week, normal users by a day
					// Maximum wait duration is 30 days, however
					val waitDays = ((numberOfOwners - 1) * 7 + (organizationSize - numberOfOwners + 1)) min 30
					// Inserts a new deletion
					organization.deletions.insert(session.userId, waitDays.days)
				}
			}
			Result.Success(deletion.toModel)
		}
	}
}