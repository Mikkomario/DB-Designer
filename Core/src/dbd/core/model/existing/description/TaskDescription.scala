package dbd.core.model.existing.description

import dbd.core.model.partial.description.TaskDescriptionData

/**
  * Represents a stored task-description-link
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class TaskDescription(id: Int, data: TaskDescriptionData[Description])
	extends StoredDescriptionLink[TaskDescriptionData[Description]]
