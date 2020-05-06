package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.enumeration.TaskType
import dbd.core.model.existing
import dbd.core.model.partial.{DescriptionData, TaskDescriptionData}

object TaskDescription extends DescriptionLinkFactory[existing.TaskDescription, TaskDescription,
	TaskDescriptionData[DescriptionData]]
{
	override def targetIdAttName = "taskId"
	
	// FIXME: Currently has to unsafely unwrap the parsed task type. Fix
	override protected def apply(id: Int, targetId: Int, description: existing.Description) =
		existing.TaskDescription(id, TaskDescriptionData(TaskType.forId(targetId).get, description))
	
	override def table = Tables.taskDescription
}

/**
  * Used for interacting with task descriptions in DB
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class TaskDescription(id: Option[Int] = None, taskId: Option[Int] = None, descriptionId: Option[Int] = None,
						   deprecatedAfter: Option[Instant] = None)
	extends DescriptionLink[existing.TaskDescription, TaskDescription.type ]
{
	override def factory = TaskDescription
	
	override def targetId = taskId
}
