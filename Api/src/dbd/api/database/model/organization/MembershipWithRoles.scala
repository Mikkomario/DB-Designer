package dbd.api.database.model.organization

import dbd.core.model.combined
import dbd.core.model.enumeration.UserRole
import utopia.vault.model.immutable.Result
import utopia.vault.nosql.factory.{Deprecatable, FromResultFactory}
import utopia.vault.util.ErrorHandling

import scala.util.{Failure, Success}

/**
  * Used for reading rich membership data from DB
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
object MembershipWithRoles extends FromResultFactory[combined.MembershipWithRoles] with Deprecatable
{
	// IMPLEMENTED	----------------------------
	
	override def nonDeprecatedCondition = OrganizationMembership.nonDeprecatedCondition &&
		OrganizationMemberRole.nonDeprecatedCondition
	
	override def table = OrganizationMembership.table
	
	override def joinedTables = Vector(roleLinkTable)
	
	override def apply(result: Result) =
	{
		// Groups rows by membership id
		result.grouped(table, roleLinkTable).flatMap { case (_, membershipData) =>
			val (membershipRow, roleLinkRows) = membershipData
			// Membership must be parseable
			OrganizationMembership(membershipRow) match
			{
				case Success(membership) =>
					// Adds role ids (parsed)
					val roles = roleLinkRows.flatMap { _(roleLinkTable)(OrganizationMemberRole.roleIdAttName).int }
						.flatMap { UserRole.forId(_).toOption }
					Some(combined.MembershipWithRoles(membership, roles.toSet))
				case Failure(error) =>
					ErrorHandling.modelParsePrinciple.handle(error)
					None
			}
		}.toVector
	}
	
	
	// COMPUTED	------------------------------
	
	private def roleLinkTable = OrganizationMemberRole.table
}
