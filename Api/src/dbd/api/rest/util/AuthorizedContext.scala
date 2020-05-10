package dbd.api.rest.util

import dbd.api.database.access
import dbd.api.database.access.single
import dbd.api.database.access.single.User
import dbd.api.model.existing
import dbd.core.database.ConnectionPool
import dbd.core.model.enumeration.TaskType
import dbd.core.util.Log
import dbd.core.util.ThreadPool.executionContext
import utopia.access.http.Status.{BadRequest, Forbidden, InternalServerError, Unauthorized}
import utopia.flow.generic.FromModelFactory
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._
import utopia.nexus.http.{Request, ServerSettings}
import utopia.nexus.rest.BaseContext
import utopia.nexus.result.Result
import utopia.vault.database.Connection

import scala.util.{Failure, Success}

/**
  * This context variation checks user authorization (when required)
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
class AuthorizedContext(request: Request)(implicit serverSettings: ServerSettings) extends BaseContext(request)
{
	/**
	  * Performs the provided function if the request has correct basic authorization (email + password)
	  * @param f Function called when request is authorized. Accepts userId + database connection. Produces an http result.
	  * @return Function result or a result indicating that the request was unauthorized. Wrapped as a response.
	  */
	def basicAuthorized(f: (Int, Connection) => Result) =
	{
		// Authorizes request with basic auth, finding user id
		val result = request.headers.basicAuthorization match
		{
			case Some(basicAuth) =>
				val (email, password) = basicAuth
				
				ConnectionPool.tryWith { implicit connection =>
					User.tryAuthenticate(email, password) match
					{
						// Performs the operation on authorized user id
						case Some(userId) => f(userId, connection)
						case None => Result.Failure(Unauthorized, "Invalid email or password")
					}
				}.getOrMap { e =>
					Log(e, s"Failed to handle request $request")
					Result.Failure(InternalServerError, e.getMessage)
				}
			case None => Result.Failure(Unauthorized, "Please provide a basic auth header with user email and password")
		}
		result.toResponse(this)
	}
	
	/**
	  * Perform the specified function if the request can be authorized using a device authentication key
	  * @param f A function called when request is authorized. Accepts device key + database connection. Produces an http result.
	  * @return Function result or a result indicating that the request was unauthorized. Wrapped as a response.
	  */
	def deviceKeyAuthorized(f: (existing.DeviceKey, Connection) => Result) =
	{
		tokenAuthorized("device authentication key", f) { (token, connection) =>
			access.single.DeviceKey.matching(token)(connection)
		}
	}
	
	/**
	  * Perform the specified function if the request can be authorized using a session key
	  * @param f A function called when request is authorized. Accepts user session + database connection. Produces an http result.
	  * @return Function result or a result indicating that the request was unauthorized. Wrapped as a response.
	  */
	def sessionKeyAuthorized(f: (existing.UserSession, Connection) => Result) =
	{
		tokenAuthorized("session key", f) { (token, connection) =>
			access.single.UserSession.matching(token)(connection)
		}
	}
	
	/**
	  * Performs the specified function if the request can be authorized using either basic authorization or a
	  * device auth key. Used device auth key will have to match the specified device id. If not, it will be invalidated
	  * as a safety measure.
	  * @param requiredDeviceId Device id that the specified key must be connected to, if present
	  * @param f Function called when request is authorized. Accepts userId + whether device key was used +
	  *          database connection. Produces an http result.
	  * @return Function result or a result indicating that the request was unauthorized. Wrapped as a response.
	  */
	def basicOrDeviceKeyAuthorized(requiredDeviceId: Int)(f: (Int, Boolean, Connection) => Result) =
	{
		// Checks whether basic or device authorization should be used
		request.headers.authorization match
		{
			case Some(authHeader) =>
				val authType = authHeader.untilFirst(" ")
				if (authType ~== "basic")
					basicAuthorized { (userId, connection) => f(userId, false, connection) }
				else if (authType ~== "bearer")
					deviceKeyAuthorized { (key, connection) =>
						// Makes sure the device id in the key matches the required device id. If not, invalidates the
						// key because it may have become compromised
						if (key.deviceId == requiredDeviceId)
							f(key.userId, true, connection)
						else
						{
							access.single.DeviceKey(key.id).invalidate()(connection)
							Result.Failure(Unauthorized,
								"The key you specified cannot be used for this resource. " +
									"Also, your key has now been invalidated and can no longer be used.")
						}
					}
				else
					Result.Failure(Unauthorized, "Only basic and bearer authorizations are supported").toResponse(this)
			case None => Result.Failure(Unauthorized, "Authorization header is required").toResponse(this)
		}
	}
	
	/**
	  * Performs the specified function if:<br>
	  * 1) The request can be authorized using a valid session key<br>
	  * 2) The authorized user is a member of the specified organization and<br>
	  * 3) The user has the right/authorization to perform the specified task within that organization
	  * @param organizationId Id of the targeted organization
	  * @param task The task the user is trying to perform
	  * @param f Function called when the user is fully authorized. Takes user session, membership id and database
	  *          connection as parameters. Returns operation result.
	  * @return An http response based either on the function result or authorization failure.
	  */
	def authorizedForTask(organizationId: Int, task: TaskType)(f: (existing.UserSession, Int, Connection) => Result) =
	{
		// Authorizes the request using a session key token
		sessionKeyAuthorized { (session, connection) =>
			implicit val c: Connection = connection
			// Makes sure the user belongs to the target organization
			single.User(session.userId).membershipIdInOrganizationWithId(organizationId).pull match
			{
				case Some(membershipId) =>
					// Makes sure the user has a right to perform the required task
					if (single.Membership(membershipId).allows(task))
						f(session, membershipId, connection)
					else
						Result.Failure(Forbidden,
							"You haven't been granted the right to perform this task within this organization")
				case None => Result.Failure(Unauthorized, "You're not a member of this organization")
			}
		}
	}
	
	/**
	  * Parses a model from the request body and uses it to produce a response
	  * @param parser Model parser
	  * @param f Function that will be called if the model was successfully parsed. Returns an http result.
	  * @tparam A Type of parsed model
	  * @return Function result or a failure result if no model could be parsed.
	  */
	def handlePost[A](parser: FromModelFactory[A])(f: A => Result) =
	{
		// Parses the post model first
		request.body.headOption match
		{
			case Some(body) =>
				body.bufferedJSONModel.contents match
				{
					case Success(model) =>
						parser(model) match
						{
							// Gives the parsed model to specified function
							case Success(parsed) => f(parsed)
							case Failure(error) => Result.Failure(BadRequest, error.getMessage)
						}
					case Failure(error) => Result.Failure(BadRequest, error.getMessage)
				}
			case None => Result.Failure(BadRequest, "Please provide a json-body with the response")
		}
	}
	
	private def tokenAuthorized[K](keyTypeName: => String, f: (K, Connection) => Result)(
		testKey: (String, Connection) => Option[K]) =
	{
		// Checks the key from token
		val result = request.headers.bearerAuthorization match
		{
			case Some(key) =>
				// Validates the device key against database
				ConnectionPool.tryWith { connection =>
					testKey(key, connection) match
					{
						case Some(authorizedKey) => f(authorizedKey, connection)
						case None => Result.Failure(Unauthorized, s"Invalid or expired $keyTypeName")
					}
				}.getOrMap { e =>
					Log(e, s"Failed to handle request $request")
					Result.Failure(InternalServerError, e.getMessage)
				}
			case None => Result.Failure(Unauthorized, s"Please provided a bearer auth hearer with a $keyTypeName")
		}
		result.toResponse(this)
	}
}
