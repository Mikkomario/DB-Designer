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
  * A rest resource for accessing invitations that are pending for the logged user
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
object MyInvitations extends Resource[AuthorizedContext]
{
	override val name = "invitations"
	
	override val allowedMethods = Vector(Get)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.sessionKeyAuthorized { (session, connection) =>
			implicit val c: Connection = connection
			// Reads invitations from DB
			val pendingInvitations = single.User(session.userId).receivedInvitations.pending
			Result.Success(pendingInvitations.map { _.toModel })
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(NotImplemented,
		message = Some("Individual invitation access hasn't been implemented yet"))
}
