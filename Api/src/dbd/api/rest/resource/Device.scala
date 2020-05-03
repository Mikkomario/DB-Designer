package dbd.api.rest.resource

import dbd.api.rest.util.AuthorizedContext
import utopia.access.http.Status.NotImplemented
import utopia.flow.util.StringExtensions._
import utopia.nexus.http.Path
import utopia.nexus.rest.ResourceSearchResult.{Error, Follow}
import utopia.nexus.rest.{Context, Resource}
import utopia.nexus.result.Result

/**
  * Used for accessing an individual device's information
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
case class Device(deviceId: Int) extends Resource[AuthorizedContext]
{
	override def name = deviceId.toString
	
	override def allowedMethods = Vector()
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
		Result.Failure(NotImplemented).toResponse
	
	override def follow(path: Path)(implicit context: AuthorizedContext) =
	{
		// Contains 'device-key' child node which allows access to long-term device specific authorization keys
		if (path.head ~== "device-key")
			Follow(DeviceKey(deviceId), path.tail)
		else
			Error(message = Some("Device only contains 'device-key' child node"))
	}
}
