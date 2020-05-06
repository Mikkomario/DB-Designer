package dbd.core.model.partial

import dbd.core.model.enumeration.TaskType
import dbd.core.model.template.DescriptionLinkLike

/**
  * Contains basic data about a task description link
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class TaskDescriptionData[+D](task: TaskType, description: D) extends DescriptionLinkLike[D]
{
	override def targetId = task.id
}
