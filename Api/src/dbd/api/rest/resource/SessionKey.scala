package dbd.api.rest.resource

import dbd.api.database.access.single
import dbd.api.database.access.single.UserSession
import dbd.api.rest.util.AuthorizedContext
import utopia.access.http.Method.{Delete, Get}
import utopia.access.http.Status.NotFound
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

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
			context.basicOrDeviceKeyAuthorized(deviceId) { (userId, deviceKeyWasUsed, connection) =>
				implicit val c: Connection = connection
				// On basic auth mode, makes sure the targeted device exists
				val isRealDevice =
				{
					if (deviceKeyWasUsed)
						true
					else
						single.Device(deviceId).isDefined
				}
				if (isRealDevice)
				{
					// If basic auth was used, may register a new user device -connection
					if (!deviceKeyWasUsed)
						single.User(userId).linkWithDeviceWithId(deviceId)
					val newSession = UserSession(userId, deviceId).start()
					// Returns the session key
					Result.Success(newSession.key)
				}
				else
					Result.Failure(NotFound, s"There doesn't exist a device with id $deviceId")
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
