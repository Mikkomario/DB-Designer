package dbd.api.rest.util

import scala.math.Ordering.Double.TotalOrdering
import dbd.api.database.access
import dbd.api.database.access.many.Languages
import dbd.api.database.access.single
import dbd.api.database.access.single.User
import dbd.api.model.existing
import dbd.core.database.ConnectionPool
import dbd.core.model.enumeration.TaskType
import dbd.core.util.Log
import dbd.core.util.ThreadPool.executionContext
import utopia.access.http.Status.{BadRequest, Forbidden, InternalServerError, Unauthorized}
import utopia.bunnymunch.jawn.JsonBunny
import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.FromModelFactory
import utopia.flow.parse.JsonParser
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
	// ATTRIBUTES	------------------------
	
	private implicit val jsonParser: JsonParser = JsonBunny
	
	
	// COMPUTED	----------------------------
	
	/**
	  * @param connection DB Connection (implicit)
	  * @return Languages that were requested in the Accept-Language header. The languages are listed from most to
	  *         least preferred. May be empty.
	  */
	def requestedLanguages(implicit connection: Connection) =
	{
		val acceptedLanguages = request.headers.acceptedLanguages.map { case (code, weight) => code.toLowerCase -> weight }
		if (acceptedLanguages.nonEmpty)
		{
			val acceptedCodes = acceptedLanguages.keySet
			// Maps codes to language ids (if present)
			val languages = Languages.forIsoCodes(acceptedCodes)
			// Orders the languages based on assigned weight
			languages.sortBy { l => -acceptedLanguages(l.isoCode.toLowerCase) }
		}
		else
			Vector()
	}
	
	
	// OTHER	----------------------------
	
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
	  * Performs the specified function if the user is authorized (using session key) and they are a member of the
	  * specified organization
	  * @param organizationId Id of the organization the user is supposed to be a member of
	  * @param f              Function called when the user is fully authorized. Takes user session, membership id and database
	  *                       connection as parameters. Returns operation result.
	  * @return An http response based either on the function result or authorization failure.
	  */
	def authorizedInOrganization(organizationId: Int)(f: (existing.UserSession, Int, Connection) => Result) =
	{
		// Authorizes the request using a session key token
		sessionKeyAuthorized { (session, connection) =>
			implicit val c: Connection = connection
			// Makes sure the user belongs to the target organization
			single.User(session.userId).membershipIdInOrganizationWithId(organizationId).pull match
			{
				case Some(membershipId) => f(session, membershipId, connection)
				case None => Result.Failure(Unauthorized, "You're not a member of this organization")
			}
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
		// Makes sure the user belongs to the organization and that they have a valid session key authorization
		authorizedInOrganization(organizationId) { (session, membershipId, connection) =>
			implicit val c: Connection = connection
			// Makes sure the user has a right to perform the required task
			if (single.Membership(membershipId).allows(task))
				f(session, membershipId, connection)
			else
				Result.Failure(Forbidden,
					"You haven't been granted the right to perform this task within this organization")
		}
	}
	
	/**
	  * Parses a value from the request body and uses it to produce a response
	  * @param f Function that will be called if the value was successfully read. Returns an http result.
	  * @return Function result or a failure result if no value could be read.
	  */
	def handlePost(f: Value => Result) =
	{
		// Parses the post body first
		request.body.headOption match
		{
			case Some(body) =>
				body.bufferedJson.contents match
				{
					case Success(value) => f(value)
					case Failure(error) => Result.Failure(BadRequest, error.getMessage)
				}
			case None => Result.Failure(BadRequest, "Please provide a json-body with the response")
		}
	}
	
	/**
	  * Parses a model from the request body and uses it to produce a response
	  * @param parser Model parser
	  * @param f Function that will be called if the model was successfully parsed. Returns an http result.
	  * @tparam A Type of parsed model
	  * @return Function result or a failure result if no model could be parsed.
	  */
	def handlePost[A](parser: FromModelFactory[A])(f: A => Result): Result =
	{
		handlePost { value =>
			value.model match
			{
				case Some(model) =>
					parser(model) match
					{
						// Gives the parsed model to specified function
						case Success(parsed) => f(parsed)
						case Failure(error) => Result.Failure(BadRequest, error.getMessage)
					}
				case None => Result.Failure(BadRequest, "Please provide a json object in the request body")
			}
		}
	}
	
	/**
	  * Parses request body into a vector of values and handles them using the specified function.
	  * For non-array bodies, wraps the body in a vector.
	  * @param f Function that will be called if a json body was present. Accepts a vector of values. Returns result.
	  * @return Function result or a failure if no value could be read
	  */
	def handleArrayPost(f: Vector[Value] => Result) = handlePost { v: Value =>
		if (v.isEmpty)
			f(Vector())
		else
			v.vector match
			{
				case Some(vector) => f(vector)
				// Wraps the value into a vector if necessary
				case None => f(Vector(v))
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
