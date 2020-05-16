package dbd.api.rest.resource.organization

import dbd.api.database.access.single.DbOrganization
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.enumeration.TaskType.CancelOrganizationDeletion
import utopia.access.http.Method.Delete
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * Used for accessing & altering organization deletions
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class OrganizationDeletionsNode(organizationId: Int) extends Resource[AuthorizedContext]
{
	override val name = "deletions"
	
	override val allowedMethods = Vector(Delete)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.authorizedForTask(organizationId, CancelOrganizationDeletion) { (session, _, connection) =>
			implicit val c: Connection = connection
			// Cancels all deletions targeted towards this organization
			val updatedDeletions = DbOrganization(organizationId).deletions.pending.cancel(session.userId)
			Result.Success(updatedDeletions.map { _.toModel })
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(
		message = Some("Deletions currently has no sub-nodes"))
}
