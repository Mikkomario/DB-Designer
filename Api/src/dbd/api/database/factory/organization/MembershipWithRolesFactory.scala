package dbd.api.database.factory.organization

import dbd.api.database.model.organization.{MemberRoleModel, MembershipModel}
import dbd.core.model.combined
import dbd.core.model.enumeration.UserRole
import utopia.vault.model.immutable.Result
import utopia.vault.nosql.factory.{Deprecatable, FromResultFactory}
import utopia.vault.sql.JoinType
import utopia.vault.util.ErrorHandling

import scala.util.{Failure, Success}

/**
  * Used for reading rich membership data from DB
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
object MembershipWithRolesFactory extends FromResultFactory[combined.organization.MembershipWithRoles] with Deprecatable
{
	// IMPLEMENTED	----------------------------
	
	override def nonDeprecatedCondition = MembershipModel.nonDeprecatedCondition &&
		MemberRoleModel.nonDeprecatedCondition
	
	override def table = MembershipModel.table
	
	override def joinType = JoinType.Left
	
	override def joinedTables = Vector(roleLinkTable)
	
	override def apply(result: Result) =
	{
		// Groups rows by membership id
		result.grouped(table, roleLinkTable).flatMap { case (_, membershipData) =>
			val (membershipRow, roleLinkRows) = membershipData
			// Membership must be parseable
			MembershipModel(membershipRow) match
			{
				case Success(membership) =>
					// Adds role ids (parsed)
					val roles = roleLinkRows.flatMap { _(roleLinkTable)(MemberRoleModel.roleIdAttName).int }
						.flatMap { UserRole.forId(_).toOption }
					Some(combined.organization.MembershipWithRoles(membership, roles.toSet))
				case Failure(error) =>
					ErrorHandling.modelParsePrinciple.handle(error)
					None
			}
		}.toVector
	}
	
	
	// COMPUTED	------------------------------
	
	private def roleLinkTable = MemberRoleModel.table
}
