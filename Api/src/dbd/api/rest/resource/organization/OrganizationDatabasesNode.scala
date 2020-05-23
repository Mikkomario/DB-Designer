package dbd.api.rest.resource.organization

import java.time.Instant

import dbd.api.database.access.single.user.DbOrganization
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.combined.ChangesList
import utopia.access.http.Method.Get
import utopia.access.http.Status.BadRequest
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.TimeExtensions._
import utopia.flow.util.StringExtensions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.{Error, Follow}
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * Used for accessing an organization's current databases
  * @author Mikko Hilpinen
  * @since 23.5.2020, v2
  */
case class OrganizationDatabasesNode(organizationId: Int) extends Resource[AuthorizedContext]
{
	// IMPLEMENTED	----------------------------
	
	override val name = "databases"
	
	override val allowedMethods = Vector(Get)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.authorizedInOrganization(organizationId) { (_, _, connection) =>
			implicit val c: Connection = connection
			// Reads database data, including current configurations
			val databases = DbOrganization(organizationId).databases.all
			Result.Success(databases.map { _.toModel })
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) =
	{
		if (path.head ~== Changes.name)
			Follow(Changes, path.tail)
		else
			Error(message = Some("databases only contains 'changes' subnode"))
	}
	
	
	// NESTED	------------------------------
	
	private object Changes extends Resource[AuthorizedContext]
	{
		override def name = "changes"
		
		override def allowedMethods = Vector(Get)
		
		override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
		{
			context.request.parameters("since").instant match
			{
				case Some(threshold) =>
					context.authorizedInOrganization(organizationId) { (_, _, connection) =>
						implicit val c: Connection = connection
						// Reads changes and additions + deletions and then combines them to a change list
						val access = DbOrganization(organizationId).databases
						val requestTime = Instant.now()
						val (modifiedDbs, newDbs) = access.createdOrModifiedAfter(threshold).divideBy { _.created > threshold }
						val deletedDbs = access.deletedAfter(threshold)
						Result.Success(ChangesList(newDbs, modifiedDbs, deletedDbs, requestTime).toModel)
					}
				case None => Result.Failure(BadRequest, "'since' timestamp -parameter required for listing changes").toResponse
			}
		}
		
		override def follow(path: Path)(implicit context: AuthorizedContext) = Error(
			message = Some("Database changes doesn't contain any sub-nodes"))
	}
}
