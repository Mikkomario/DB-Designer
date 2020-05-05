package dbd.api.rest.resource.organization

import dbd.api.rest.util.AuthorizedContext
import utopia.access.http.Status.NotImplemented
import utopia.flow.util.StringExtensions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.{Error, Follow}
import utopia.nexus.result.Result

/**
  * A rest resource for accessing individual organization's data
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class Organization(organizationId: Int) extends Resource[AuthorizedContext]
{
	override def name = organizationId.toString
	
	// At this time, no methods are allowed for organization
	override val allowedMethods = Vector()
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) = Result.Failure(
		NotImplemented, "Organization resource access hasn't been implemented yet").toResponse
	
	override def follow(path: Path)(implicit context: AuthorizedContext) =
	{
		if (path.head ~== "invitations")
			Follow(Invitations(organizationId), path.tail)
		else
			Error(message = Some("Organization only contains 'invitations' -sub-resource at this time"))
	}
}
