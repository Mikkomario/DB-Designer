package dbd.api.rest.resource.user

import dbd.api.rest.util.AuthorizedContext
import utopia.access.http.Status.NotImplemented
import utopia.flow.util.StringExtensions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.{Error, Follow}
import utopia.nexus.result.Result

/**
  * This rest-resource represents the logged user
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
object Me extends Resource[AuthorizedContext]
{
	override val name = "me"
	
	override val allowedMethods = Vector()
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) = Result.Failure(
		NotImplemented, "User data accessing hasn't been implemented yet").toResponse
	
	override def follow(path: Path)(implicit context: AuthorizedContext) =
	{
		if (path.head ~== "invitations")
			Follow(MyInvitations, path.tail)
		else
			Error(message = Some("Current user only contains sub-resource 'invitations' at this time"))
	}
}
