package dbd.api.database.access.many

import java.time.Instant

import dbd.api.database.model.{Invitation, InvitationResponse, InvitationWithResponse}
import dbd.core.model.existing
import utopia.vault.database.Connection
import utopia.vault.model.enumeration.ComparisonOperator.Larger
import utopia.vault.nosql.access.ManyModelAccess
import utopia.vault.sql.{JoinType, Select, Where}

/**
  * Common trait for multiple invitations -access points
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
trait InvitationsAccess extends ManyModelAccess[existing.Invitation]
{
	// IMPLEMENTED	------------------------
	
	override def factory = Invitation
	
	
	// COMPUTED	-----------------------------
	
	/**
	  * @param connection DB Connection (implicit)
	  * @return Invitations that have been blocked
	  */
	def blocked(implicit connection: Connection) =
	{
		val additionalCondition = InvitationResponse.blocked.toCondition
		InvitationWithResponse.getMany(mergeCondition(additionalCondition))
	}
	
	/**
	  * @param connection DB Connection (implicit)
	  * @return Invitations that are currently without response
	  */
	def pending(implicit connection: Connection) =
	{
		// Pending invitations must not be joined to a response and not be expired
		val noResponseCondition = InvitationResponse.table.primaryColumn.get.isNull
		val pendingCondition = Invitation.withExpireTime(Instant.now()).toConditionWithOperator(Larger)
		// Has to join invitation response table for the condition to work
		connection(Select(Invitation.target.join(InvitationResponse.table, JoinType.Left), Invitation.table) +
			Where(mergeCondition(noResponseCondition && pendingCondition))).parse(factory)
	}
}
