package dbd.api.rest.resource.device

import dbd.api.database.access.single
import dbd.api.rest.util.AuthorizedContext
import utopia.access.http.Method.{Delete, Get}
import utopia.access.http.Status.NotFound
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.nexus.result.Result.Empty
import utopia.vault.database.Connection

/**
  * This resource is used for acquiring long-term device authorization keys
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
case class DeviceKey(deviceId: Int) extends Resource[AuthorizedContext]
{
	// IMPLEMENTED	-------------------------------
	
	override val name = "device-key"
	
	override val allowedMethods = Vector(Get, Delete)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		// On GET, generates a new device key for the authorized (basic auth) user,
		// releasing the key from other device users
		if (context.request.method == Get)
		{
			context.basicAuthorized { (userId, connection) =>
				implicit val c: Connection = connection
				// Makes sure this device exists
				if (single.Device(deviceId).isDefined)
				{
					// Registers a new connection between the user and this device, if there wasn't one already
					single.User(userId).linkWithDeviceWithId(deviceId)
					// Gets and returns the new device authentication key
					val key = single.Device(deviceId).authenticationKey.assignToUserWithId(userId).key
					Result.Success(key)
				}
				else
					Result.Failure(NotFound, s"There exists no device with id $deviceId")
			}
		}
		// On DELETE, deprecates the authorized user's (session auth) device key for this device
		else
		{
			context.sessionKeyAuthorized { (session, connection) =>
				implicit val c: Connection = connection
				single.Device(session.deviceId).authenticationKey.releaseFromUserWithId(session.userId)
				Empty
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(message = Some(s"$name doesn't have any children"))
}