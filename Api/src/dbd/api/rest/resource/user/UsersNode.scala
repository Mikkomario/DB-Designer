package dbd.api.rest.resource.user

import dbd.api.database.access.many
import dbd.api.rest.util.AuthorizedContext
import dbd.core.database.ConnectionPool
import dbd.core.model.error.AlreadyUsedException
import dbd.core.model.post.NewUser
import dbd.core.util.{Log, ThreadPool}
import utopia.access.http.Method.Post
import utopia.access.http.Status._
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.StringExtensions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.{Error, Follow}
import utopia.nexus.result.Result

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * A rest-resource for all users
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object UsersNode extends Resource[AuthorizedContext]
{
	override val name = "users"
	
	override val allowedMethods = Vector(Post)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		// Parses the post model first
		context.handlePost(NewUser) { newUser =>
			implicit val exc: ExecutionContext = ThreadPool.executionContext
			// Saves the new user data to DB
			ConnectionPool.tryWith { implicit connection =>
				many.DbUsers.tryInsert(newUser) match
				{
					case Success(userData) =>
						// Returns a summary of the new data
						Result.Success(userData.toModel, Created)
					case Failure(error) =>
						error match
						{
							case a: AlreadyUsedException => Result.Failure(Forbidden, a.getMessage)
							case _ => Result.Failure(BadRequest, error.getMessage)
						}
				}
			} match
			{
				case Success(result) => result
				case Failure(error) =>
					Log(error, s"Failed to save $newUser to $name")
					Result.Failure(InternalServerError)
			}
		}.toResponse
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) =
	{
		if (path.head ~== "me")
			Follow(MeNode, path.tail)
		else
			Error(message = Some(s"Currently only 'me' is available under $name"))
	}
}
