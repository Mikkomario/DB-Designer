package dbd.api.database.access.many

import dbd.api.database.model.RoleRight
import dbd.core.model.enumeration.UserRole
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
}
