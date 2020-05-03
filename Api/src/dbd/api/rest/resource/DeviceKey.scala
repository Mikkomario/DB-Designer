package dbd.api.rest.resource

import dbd.api.database.access.single.{Device, User}
import dbd.core.database.ConnectionPool
import dbd.core.util.Log
import dbd.core.util.ThreadPool.executionContext
import utopia.access.http.Method.{Delete, Get}
import utopia.access.http.Status.{InternalServerError, Unauthorized}
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.CollectionExtensions._
import utopia.nexus.http.Path
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.rest.{Context, Resource}
import utopia.nexus.result.Result

/**
  * This resource is used for acquiring long-term device authorization keys
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
case class DeviceKey(deviceId: Int) extends Resource[Context]
{
	// IMPLEMENTED	-------------------------------
	
	override val name = "device-key"
	
	override val allowedMethods = Vector(Get, Delete)
	
	// TODO: Implement delete
	override def toResponse(remainingPath: Option[Path])(implicit context: Context) =
	{
		// Checks authentication (requires basic auth with user email + password)
		val result = context.request.headers.basicAuthorization match
		{
			case Some(basicAuth) =>
				val (email, password) = basicAuth
				
				ConnectionPool.tryWith { implicit connection =>
					User.tryAuthenticate(email, password) match
					{
						case Some(userId) =>
							// Gets and returns the new device authentication key
							val key = Device(deviceId).authenticationKey.assignToUserWithId(userId).key
							Result.Success(key)
						case None => Result.Failure(Unauthorized, "Invalid email or password")
					}
				}.getOrMap { e =>
					Log(e, s"Failed to get device authentication key for device $deviceId")
					Result.Failure(InternalServerError, e.getMessage)
				}
			case None => Result.Failure(Unauthorized, "Please provide a basic auth header with user email and password")
		}
		result.toResponse
	}
	
	override def follow(path: Path)(implicit context: Context) = Error(message = Some(s"$name doesn't have any children"))
}
