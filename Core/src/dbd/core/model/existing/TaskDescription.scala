package dbd.core.model.existing

import dbd.core.model.partial.TaskDescriptionData

/**
  * Represents a stored task-description-link
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class TaskDescription(id: Int, data: TaskDescriptionData[Description])
	extends StoredDescriptionLink[TaskDescriptionData[Description]]
