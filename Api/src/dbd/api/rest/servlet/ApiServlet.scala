package dbd.api.rest.servlet

import dbd.api.rest.resource.device.Devices
import dbd.api.rest.resource.organization.Organizations
import dbd.api.rest.resource.user.Users
import dbd.api.rest.util.AuthorizedContext
import javax.servlet.annotation.MultipartConfig
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import utopia.access.http.Status.BadRequest
import utopia.bunnymunch.jawn.JsonBunny
import utopia.flow.generic.DataType
import utopia.flow.parse.JsonParser
import utopia.nexus.http.{Path, ServerSettings}
import utopia.nexus.rest.RequestHandler
import utopia.nexus.servlet.HttpExtensions._
import utopia.vault.database.Connection
import utopia.vault.util.ErrorHandling
import utopia.vault.util.ErrorHandlingPrinciple.Throw

/**
  * A servlet that serves the API
  * @author Mikko Hilpinen
  * @since 3.5.2020, v2
  */
@MultipartConfig(
	fileSizeThreshold   = 1048576,  // 1 MB
	maxFileSize         = 10485760, // 10 MB
	maxRequestSize      = 20971520, // 20 MB
)
class ApiServlet extends HttpServlet
{
	// INITIAL CODE	----------------------------
	
	DataType.setup()
	Connection.modifySettings { _.copy(driver = Some("org.mariadb.jdbc.Driver")) }
	// TODO: Change this once more advanced logging systems are available and in production
	ErrorHandling.defaultPrinciple = Throw
	
	
	// ATTRIBUTES	----------------------------
	
	// TODO: When going to production, read these from settings and maybe use parameter encoding
	private implicit val serverSettings: ServerSettings = ServerSettings("http://localhost:9999")
	private implicit val jsonParser: JsonParser = JsonBunny
	
	private val handler = new RequestHandler(Vector(Users, Devices, Organizations), Some(Path("db-designer", "api", "v1")),
		r => new AuthorizedContext(r))
	
	
	// IMPLEMENTED	----------------------------
	
	override def doGet(req: HttpServletRequest, resp: HttpServletResponse) = handleRequest(req, resp)
	
	override def doPost(req: HttpServletRequest, resp: HttpServletResponse) = handleRequest(req, resp)
	
	override def doPut(req: HttpServletRequest, resp: HttpServletResponse) = handleRequest(req, resp)
	
	override def doDelete(req: HttpServletRequest, resp: HttpServletResponse) = handleRequest(req, resp)
	
	
	// OTHER	--------------------------------
	
	private def handleRequest(request: HttpServletRequest, response: HttpServletResponse) =
	{
		request.toRequest match
		{
			case Some(parseRequest) =>
				val newResponse = handler(parseRequest)
				newResponse.update(response)
			case None => response.setStatus(BadRequest.code)
		}
	}
}
