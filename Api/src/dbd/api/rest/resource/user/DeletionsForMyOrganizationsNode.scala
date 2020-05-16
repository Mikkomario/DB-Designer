package dbd.api.rest.resource.user

import dbd.api.rest.resource.ResourceWithChildren
import dbd.api.rest.util.AuthorizedContext
import utopia.access.http.Status.NotImplemented
import utopia.nexus.http.Path
import utopia.nexus.result.Result

/**
  * Used for accessing deletions concerning the organizations the current user is a member of
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
object DeletionsForMyOrganizationsNode extends ResourceWithChildren[AuthorizedContext]
{
	override def children = Vector(PendingDeletionsForMyOrganizationsNode)
	
	override val name = "deletions"
	
	override def allowedMethods = Vector()
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
		Result.Failure(NotImplemented, "Deletions access is not implemented at this time").toResponse
}