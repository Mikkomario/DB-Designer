package dbd.api.rest.resource.user

import dbd.api.database.access.single.DbUser
import dbd.api.rest.resource.ResourceWithChildren
import dbd.api.rest.util.AuthorizedContext
import dbd.core.util.Log
import utopia.access.http.Method.Get
import utopia.access.http.Status.Unauthorized
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * This rest-resource represents the logged user
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
object MeNode extends ResourceWithChildren[AuthorizedContext]
{
	override val name = "me"
	
	override val children = Vector(MyInvitationsNode, MyOrganizationsNode, MyLanguagesNode)
	
	override val allowedMethods = Vector(Get)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.sessionKeyAuthorized { (session, connection) =>
			implicit val c: Connection = connection
			// Reads user data and adds linked data
			DbUser(session.userId).withLinks match
			{
				case Some(user) => Result.Success(user.toModel)
				case None =>
					Log.warning(s"User id ${session.userId} was authorized but couldn't be found from the database")
					Result.Failure(Unauthorized, "User no longer exists")
			}
		}
	}
}
