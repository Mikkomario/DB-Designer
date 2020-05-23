package dbd.api.rest.resource.description

import dbd.api.database.access.many.description.DbDescriptions
import dbd.api.database.access.many.user.DbUserRoles
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.combined.organization.DescribedRole
import utopia.access.http.Method.Get
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * This node is used for listing all possible user roles and their descriptions, including associated task ids
  * @author Mikko Hilpinen
  * @since 20.5.2020, v2
  */
object RolesNode extends Resource[AuthorizedContext]
{
	override val name = "user-roles"
	
	override val allowedMethods = Vector(Get)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.sessionKeyAuthorized { (session, connection) =>
			implicit val c: Connection = connection
			val languageIds = context.languageIdListFor(session.userId)
			// Reads all user roles and their allowed tasks
			val roles = DbUserRoles.withRights
			// Reads role descriptions and combines them with roles
			val descriptions = DbDescriptions.ofAllRoles.inLanguages(languageIds)
			val rolesWithDescriptions = roles.map { role =>
				DescribedRole(role, descriptions.getOrElse(role.id, Set()).toSet) }
			Result.Success(rolesWithDescriptions.map { _.toModel })
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(message = Some(
		"user-roles doesn't contain any sub-nodes at this time"))
}