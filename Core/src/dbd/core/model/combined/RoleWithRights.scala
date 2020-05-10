package dbd.core.model.combined

import dbd.core.model.enumeration.{TaskType, UserRole}
import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Contains a role + all tasks that are linked to that role
  * @author Mikko Hilpinen
  * @since 10.5.2020, v2
  * @constructor Links rights with a role
  * @param role Described role
  * @param tasks Tasks available to that role
  */
case class RoleWithRights(role: UserRole, tasks: Set[TaskType]) extends ModelConvertible
{
	override def toModel = Model(Vector("id" -> role.id,
		"task_ids" -> tasks.map { _.id }.toVector.sorted))
}
