package dbd.api.database.access.single

import dbd.api.database
import dbd.api.database.Tables
import dbd.api.database.access.many.TaskTypes
import dbd.core.model.enumeration.{TaskType, UserRole}
import dbd.core.model.existing
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleIdModelAccess, SingleModelAccess}
import utopia.vault.sql.{Exists, Select, SelectDistinct, Where}

/**
  * An access point to individual memberships and their data
  * @author Mikko Hilpinen
  * @since 5.5.2020, v2
  */
object Membership extends SingleModelAccess[existing.Membership]
{
	// IMPLEMENTED	--------------------------
	
	override def factory = database.model.OrganizationMembership
	
	override def globalCondition = Some(factory.nonDeprecatedCondition)
	
	
	// OTHER	------------------------------
	
	/**
	  * @param id A membership id
	  * @return An access point to that membership
	  */
	def apply(id: Int) = new SingleMembership(id)
	
	
	// NESTED	------------------------------
	
	class SingleMembership(membershipId: Int) extends SingleIdModelAccess[existing.Membership](membershipId, factory)
	{
		// COMPUTED	--------------------------
		
		private def memberRoleFactory = database.model.OrganizationMemberRole
		
		private def rightsFactory = database.model.RoleRight
		
		private def rightsTarget = memberRoleFactory.table join Tables.userRole join rightsFactory.table
		
		/**
		  * @param connection DB Connection (implicit)
		  * @return Roles assigned to this user in this membership
		  */
		def roles(implicit connection: Connection) =
		{
			connection(Select.index(memberRoleFactory.table) + Where(rolesCondition)).rowIntValues.flatMap { roleId =>
				UserRole.forId(roleId).toOption }
		}
		
		/**
		  * @param connection DB Connection (implicit)
		  * @return All tasks that are allowed through this membership
		  */
		def allowedActions(implicit connection: Connection) =
		{
			// Joins in the tasks link table and selects unique task ids
			connection(SelectDistinct(rightsTarget, rightsFactory.taskIdColumn) + Where(rolesCondition)).rowIntValues
				.flatMap { taskId => TaskType.forId(taskId).toOption }
		}
		
		private def rolesCondition = memberRoleFactory.withMembershipId(membershipId).toCondition &&
			memberRoleFactory.nonDeprecatedCondition
		
		
		// OTHER	---------------------------
		
		/**
		  * Checks whether this membership allows the specified action
		  * @param action Action the user would like to perform
		  * @param connection DB Connection (implicit)
		  * @return Whether the user/member is allowed to perform the specified task in this organization/membership
		  */
		def allows(action: TaskType)(implicit connection: Connection) =
		{
			Exists(rightsTarget, rolesCondition && rightsFactory.withTask(action).toCondition)
		}
		
		/**
		  * Checks whether this organization member can promote another user to specified role. This member needs to
		  * have all the rights the targeted role would have
		  * @param targetRole A role a user is being promoted to
		  * @param connection DB Connection (implicit)
		  * @return Whether this member has the authorization to promote a user to that role
		  */
		def canPromoteTo(targetRole: UserRole)(implicit connection: Connection) =
		{
			// Uses multiple requests since the join logic is rather complex
			val myTasks = allowedActions.toSet
			val requiredTasks = TaskTypes.forRole(targetRole).toSet
			requiredTasks.forall(myTasks.contains)
		}
	}
}
