package dbd.api.rest.resource

import dbd.core.model.post.NewUser
import utopia.access.http.Method.Post
import utopia.access.http.Status.NotImplemented
import utopia.nexus.http.Path
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.rest.{Context, Resource}

import scala.util.{Failure, Success}

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
	override def toResponse(remainingPath: Option[Path])(implicit context: Context) =
	{
		// Parses the post model first
		context.request.body.headOption match
		{
			case Some(body) =>
				body.bufferedJSONModel.contents match
				{
					case Success(model) =>
						NewUser(model) match
						{
							case Success(newUser) =>
								// Saves the new user data to DB
								// Returns a summary of the new data
							case Failure(error) =>
						}
					case Failure(error) =>
				}
			case None =>
		}
		
		???
	}
	
	override def follow(path: Path)(implicit context: Context) = Error(NotImplemented,
		Some("Access to user data hasn't been implemented yet"))
}
