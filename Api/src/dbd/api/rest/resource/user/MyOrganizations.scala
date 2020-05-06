package dbd.api.rest.resource.user

import dbd.api.database.access.single
import dbd.api.rest.util.AuthorizedContext
import utopia.access.http.Method.Get
import utopia.access.http.Status.NotImplemented
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * This rest node returns a descriptive list of all the organizations the authorized user belongs to
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
object MyOrganizations extends Resource[AuthorizedContext]
{
	// IMPLEMENTED	------------------------
	
	override val name = "organizations"
	
	override val allowedMethods = Vector(Get)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.sessionKeyAuthorized { (session, connection) =>
			implicit val c: Connection = connection
			// Reads organizations data and returns it as an array
			val organizations = single.User(session.userId).memberships.myOrganizations
			Result.Success(organizations.map { _.toModel })
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(NotImplemented,
		Some("Access to individual user's organizations hasn't yet been implemented"))
}
