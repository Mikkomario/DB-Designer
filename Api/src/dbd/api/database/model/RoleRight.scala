package dbd.api.database.model

import dbd.api.database.Tables
import dbd.api.model.existing
import dbd.core.model.enumeration.{TaskType, UserRole}
import utopia.flow.datastructure.template.{Model, Property}
import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.StorableFactory

object RoleRight extends StorableFactory[existing.RoleRight]
{
	// ATTRIBUTES	--------------------------
	
	/**
	  * Name of the attribute that holds the role id
	  */
	val roleIdAttName = "roleId"
	
	/**
	  * Name of the attribute that holds the task id
	  */
	val taskIdAttName = "taskId"
	
	
	// IMPLEMENTED	--------------------------
	
	/**
	  * @return Table used by this class/object
	  */
	def table = Tables.roleRight
	
	override def apply(model: Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		// Both enumeration values must be parseable
		UserRole.forId(valid(roleIdAttName).getInt).flatMap { role =>
			TaskType.forId(valid("taskId").getInt).map { task => existing.RoleRight(valid("id").getInt, role, task) }
		}
	}
	
	
	// COMPUTED	------------------------------
	
	/**
	  * @return Column that holds the role id
	  */
	def roleIdColumn = table(roleIdAttName)
	
	/**
	  * @return Column that holds the task id
	  */
	def taskIdColumn = table(taskIdAttName)
	
	
	// OTHER	------------------------------
	
	/**
	  * @param role A user role
	  * @return A model with only that role set
	  */
	def withRole(role: UserRole) = apply(role = Some(role))
	
	/**
	  * @param task A task type
	  * @return A model with only task type set
	  */
	def withTask(task: TaskType) = apply(task = Some(task))
}

/**
  * Used for interacting with role-task -links in DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class RoleRight(id: Option[Int] = None, role: Option[UserRole] = None, task: Option[TaskType] = None)
	extends StorableWithFactory[existing.RoleRight]
{
	import RoleRight._
	
	// IMPLEMENTED	----------------------
	
	override def factory = RoleRight
	
	override def valueProperties = Vector("id" -> id, roleIdAttName -> role.map { _.id }, taskIdAttName -> task.map { _.id })
}
