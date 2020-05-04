package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.enumeration.UserRole
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.factory.Deprecatable

object OrganizationMemberRole extends Deprecatable
{
	// COMPUTED	----------------------------
	
	def table = Tables.organizationMemberRole
	
	
	// IMPLEMENTED	------------------------
	
	override val nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	
	// OTHER	----------------------------
	
	/**
	  * Inserts a new membership-role -connection to the DB
	  * @param membershipId Id of associated organization membership
	  * @param role Role assigned to the user in the organization
	  * @param creatorId Id of the user who created this link
	  * @param connection DB Connection (implicit)
	  * @return Id of the newly inserted link
	  */
	def insert(membershipId: Int, role: UserRole, creatorId: Int)(implicit connection: Connection) =
		apply(None, Some(membershipId), Some(role), Some(creatorId)).insert().getInt
}

/**
  * Links organization memberships with their roles
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class OrganizationMemberRole(id: Option[Int] = None, membershipId: Option[Int] = None,
								  role: Option[UserRole] = None, creatorId: Option[Int] = None,
								  deprecatedAfter: Option[Instant] = None) extends Storable
{
	override def table = OrganizationMemberRole.table
	
	override def valueProperties = Vector("id" -> id, "membershipId" -> membershipId, "roleId" -> role.map { _.id },
		"creatorId" -> creatorId, "deprecatedAfter" -> deprecatedAfter)
}
