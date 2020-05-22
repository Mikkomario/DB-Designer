package dbd.api.rest.resource.description

import dbd.api.database.access.many.description.DbDescriptions
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.combined.organization.DescribedTask
import dbd.core.model.enumeration.TaskType
import utopia.access.http.Method.Get
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * Used for describing all available task types
  * @author Mikko Hilpinen
  * @since 21.5.2020, v2
  */
object TasksNode extends Resource[AuthorizedContext]
{
	override val name = "tasks"
	
	override val allowedMethods = Vector(Get)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.sessionKeyAuthorized { (session, connection) =>
			implicit val c: Connection = connection
			val languageIds = context.languageIdListFor(session.userId)
			// Reads task descriptions
			val descriptions = DbDescriptions.ofAllTasks.inLanguages(languageIds)
			// Combines the descriptions with the tasks and returns them
			val describedTasks = TaskType.values.map { task => DescribedTask(task,
				descriptions.getOrElse(task.id, Set()).toSet) }
			Result.Success(describedTasks.map { _.toModel })
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(message = Some(
		"Tasks currently doesn't contain any sub-nodes"))
}
