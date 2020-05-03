package dbd.api.rest.util

import dbd.api.database.access
import dbd.api.database.access.single.User
import dbd.api.model.existing
import dbd.core.database.ConnectionPool
import dbd.core.util.Log
import dbd.core.util.ThreadPool.executionContext
import utopia.access.http.Status.{InternalServerError, Unauthorized}
import utopia.flow.util.CollectionExtensions._
import utopia.nexus.http.{Request, ServerSettings}
import utopia.nexus.rest.BaseContext
import utopia.nexus.result.Result
import utopia.vault.database.Connection

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
		// Checks device key from token
		val result = request.headers.bearerAuthorization match
		{
			case Some(deviceKey) =>
				// Validates the device key against database
				ConnectionPool.tryWith { implicit connection =>
					access.single.DeviceKey.matching(deviceKey) match
					{
						case Some(authorizedKey) => f(authorizedKey, connection)
						case None => Result.Failure(Unauthorized, "Invalid or expired device key")
					}
				}.getOrMap { e =>
					Log(e, s"Failed to handle request $request")
					Result.Failure(InternalServerError, e.getMessage)
				}
			case None => Result.Failure(Unauthorized, "Please provided a bearer auth hearer with a device authentication key")
		}
		result.toResponse(this)
	}
}
