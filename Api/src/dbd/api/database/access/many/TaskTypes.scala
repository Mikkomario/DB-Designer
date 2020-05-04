package dbd.api.database.access.many

import dbd.api.database.model.RoleRight
import dbd.core.model.enumeration.UserRole
import utopia.vault.database.Connection

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
}
