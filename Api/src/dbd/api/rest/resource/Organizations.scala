package dbd.api.rest.resource

import dbd.api.database.access.many
import dbd.api.database.access.single.Language
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.post.NewOrganization
import utopia.access.http.Method.Post
import utopia.access.http.Status.{Created, NotFound, NotImplemented}
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * Used for accessing organization data via REST API interface
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object Organizations extends Resource[AuthorizedContext]
{
	// IMPLEMENTED	------------------------------
	
	override val name = "organizations"
	
	override val allowedMethods = Vector(Post)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.sessionKeyAuthorized { (session, connection) =>
			context.handlePost(NewOrganization) { newOrganization =>
				implicit val c: Connection = connection
				// Checks that language id is valid, then inserts the new organization
				if (Language(newOrganization.languageId).isDefined)
				{
					val inserted = many.Organizations.insert(newOrganization.name, newOrganization.languageId, session.userId)
					Result.Success(inserted.organizationId, Created)
				}
				else
					Result.Failure(NotFound, s"There doesn't exist a language with id ${newOrganization.languageId}")
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(NotImplemented,
		Some("Individual organization data access hasn't been implemented yet"))
}
