package dbd.api.database.model.description

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.enumeration.TaskType
import dbd.core.model.existing.description
import dbd.core.model.existing.description.Description
import dbd.core.model.{existing, partial}
import dbd.core.model.partial.description.{DescriptionData, TaskDescriptionData}

object TaskDescriptionModel extends DescriptionLinkFactory[description.TaskDescription, TaskDescriptionModel,
	TaskDescriptionData[DescriptionData]]
{
	override def targetIdAttName = "taskId"
	
	override protected def apply(id: Int, targetId: Int, description: Description) =
		TaskType.forId(targetId).map { task => existing.description.TaskDescription(id, partial.description.TaskDescriptionData(task, description)) }
	
	override def table = Tables.taskDescription
}

/**
  * Used for interacting with task descriptions in DB
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class TaskDescriptionModel(id: Option[Int] = None, taskId: Option[Int] = None, descriptionId: Option[Int] = None,
								deprecatedAfter: Option[Instant] = None)
	extends DescriptionLinkModel[description.TaskDescription, TaskDescriptionModel.type ]
{
	override def factory = TaskDescriptionModel
	
	override def targetId = taskId
}
