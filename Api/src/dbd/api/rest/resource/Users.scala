package dbd.api.rest.resource

import dbd.api.database.access.many
import dbd.core.database.ConnectionPool
import dbd.core.model.error.AlreadyUsedException
import dbd.core.model.post.NewUser
import dbd.core.util.{Log, ThreadPool}
import utopia.access.http.Method.Post
import utopia.access.http.Status.{BadRequest, Created, Forbidden, InternalServerError, NotImplemented}
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.rest.{Context, Resource}
import utopia.nexus.result.Result

import scala.concurrent.ExecutionContext
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
	
	override def toResponse(remainingPath: Option[Path])(implicit context: Context) =
	{
		// Parses the post model first
		val result = context.request.body.headOption match
		{
			case Some(body) =>
				body.bufferedJSONModel.contents match
				{
					case Success(model) =>
						NewUser(model) match
						{
							case Success(newUser) =>
								implicit val exc: ExecutionContext = ThreadPool.executionContext
								// Saves the new user data to DB
								ConnectionPool.tryWith { implicit connection =>
									many.Users.tryInsert(newUser) match
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
							case Failure(error) => Result.Failure(BadRequest, error.getMessage)
						}
					case Failure(error) => Result.Failure(BadRequest, error.getMessage)
				}
			case None => Result.Failure(BadRequest, "Please provide a json-body with the response")
		}
		result.toResponse
	}
	
	override def follow(path: Path)(implicit context: Context) = Error(NotImplemented,
		Some("Access to user data hasn't been implemented yet"))
}
