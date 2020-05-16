package dbd.api.database.access.many

import dbd.api.database.model.organization.RoleRight
import dbd.core.model.enumeration.UserRole
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.sql.Extensions._

/**
  * Used for accessing multiple task types at once
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object TaskTypes
{
	/**
	  * @param role A user role
	  * @param connection DB Connection (implicit)
	  * @return All task types that are accessible for that user role
	  */
	def forRole(role: UserRole)(implicit connection: Connection) =
	{
		// Reads task types from role rights
		RoleRight.getMany(RoleRight.withRole(role).toCondition).map { _.task }
	}
	
	/**
	  * @param roles A set of roles
	  * @param connection DB Connection
	  * @return All task types that are accessible for any of these roles
	  */
	def forRoleCombination(roles: Set[UserRole])(implicit connection: Connection) =
	{
		if (roles.isEmpty)
			Vector()
		else if (roles.size == 1)
			forRole(roles.head)
		else
			RoleRight.getMany(RoleRight.roleIdColumn.in(roles.map { _.id })).map { _.task }
	}
}
