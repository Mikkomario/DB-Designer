package dbd.core.model.combined

import dbd.core.model.enumeration.TaskType
import dbd.core.model.existing.TaskDescription

/**
  * Combines task type with some or all of its descriptions
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  * @param task Wrapped task
  * @param descriptions Various descriptions for this task
  */
case class DescribedTask(task: TaskType, descriptions: Set[TaskDescription])
