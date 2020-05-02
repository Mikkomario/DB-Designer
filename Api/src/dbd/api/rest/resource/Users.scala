package dbd.api.rest.resource

import utopia.access.http.Method.Post
import utopia.access.http.Status.NotImplemented
import utopia.nexus.http.Path
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.rest.{Context, Resource}

/**
  * A rest-resource for all users
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object Users extends Resource[Context]
{
	override val name = "users"
	
	override val allowedMethods = Vector(Post)
	
	// TODO: Implement
	override def toResponse(remainingPath: Option[Path])(implicit context: Context) = ???
	
	override def follow(path: Path)(implicit context: Context) = Error(NotImplemented,
		Some("Access to user data hasn't been implemented yet"))
}
