package dbd.api.rest.resource

import dbd.api.database.access.single.UserSession
import dbd.api.rest.util.AuthorizedContext
import utopia.access.http.Method.{Delete, Get}
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result

/**
  * Used for accessing temporary session-keys, which are used for authorizing requests
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
case class SessionKey(deviceId: Int) extends Resource[AuthorizedContext]
{
	override val name = "session-key"
	
	override val allowedMethods = Vector(Get, Delete)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		// GET retrieves a new temporary session key, invalidating any existing session keys for the user device -combination
		if (context.request.method == Get)
		{
			// Authorizes the request using either device key or basic authorization
			context.basicOrDeviceKeyAuthorized(deviceId) { (userId, connection) =>
				val newSession = UserSession(userId, deviceId).start()(connection)
				// Returns the session key
				Result.Success(newSession.key)
			}
		}
		// DELETE invalidates the session key used (authorized with a session key)
		else
		{
			context.sessionKeyAuthorized { (session, connection) =>
				UserSession(session.userId, deviceId).end()(connection)
				Result.Empty
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(
		message = Some(s"$name doesn't have any children"))
}
