package dbd.api.rest.resource.organization

import dbd.api.database.access.single
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.enumeration.TaskType.DocumentOrganization
import dbd.core.model.post.NewDescription
import utopia.access.http.Method.Put
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.vault.database.Connection

/**
  * A rest-resource used for accessing and updating organization descriptions
  * @author Mikko Hilpinen
  * @since 10.5.2020, v2
  */
case class OrganizationDescriptions(organizationId: Int) extends Resource[AuthorizedContext]
{
	override val name = "descriptions"
	
	override val allowedMethods = Vector(Put)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		// Authorizes the request and parses posted description(s)
		context.authorizedForTask(organizationId, DocumentOrganization) { (session, _, connection) =>
			context.handlePost(NewDescription) { newDescription =>
				implicit val c: Connection = connection
				// Updates the organization's descriptions accordingly
				single.Organization(organizationId).descriptions.update(newDescription, session.userId)
				// TODO: Return new version of organization's descriptions (in specified language)
				???
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(message =
		Some("Organization descriptions doesn't have any sub-resources at this time"))
}
