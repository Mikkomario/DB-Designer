package dbd.api.database.access.many

import dbd.api.database.model.RoleRight
import dbd.core.model.enumeration.{TaskType, UserRole}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.sql.{SelectDistinct, Where}
import utopia.vault.sql.Extensions._

/**
  * Used for accessing multiple user roles at once
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object UserRoles
{
	/**
	  * @param role A user role
	  * @param connection DB Connection (implicit)
	  * @return A list of user roles that have same or fewer rights that this role and can thus be considered to be
	  *         "below" this role.
	  */
	def belowOrEqualTo(role: UserRole)(implicit connection: Connection) =
	{
		val allowedTasks = TaskTypes.forRole(role)
		// Only includes roles that have only tasks within "allowed tasks" list
		val excludedRoleIds = connection(SelectDistinct(RoleRight.table, RoleRight.roleIdAttName) +
			Where.not(RoleRight.taskIdColumn.in(allowedTasks.map { _.id }))).rowIntValues
		UserRole.values.filterNot { role => excludedRoleIds.contains(role.id) }
	}
	
	/**
	  * @param roles A set of roles
	  * @param connection DB Connection
	  * @return List of roles that allow all of, or a subset of tasks allowed for any of the specified roles
	  */
	def belowOrEqualTo(roles: Set[UserRole])(implicit connection: Connection) =
		allowingOnly(TaskTypes.forRoleCombination(roles).toSet)
	
	/**
	  * @param tasks Set of allowed tasks
	  * @param connection DB Connection (implicit)
	  * @return List of user roles that allow all of, or a subset of the specified tasks
	  */
	def allowingOnly(tasks: Set[TaskType])(implicit connection: Connection) =
	{
		// Only includes roles that have only tasks within "allowed tasks" list
		val excludedRoleIds = connection(SelectDistinct(RoleRight.table, RoleRight.roleIdAttName) +
			Where.not(RoleRight.taskIdColumn.in(tasks.map { _.id }))).rowIntValues
		UserRole.values.filterNot { role => excludedRoleIds.contains(role.id) }
	}
}
