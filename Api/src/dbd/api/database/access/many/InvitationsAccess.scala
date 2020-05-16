package dbd.api.database.access.many

import java.time.Instant

import dbd.api.database.factory.organization.InvitationWithResponseFactory
import dbd.api.database.model.organization.{InvitationModel, InvitationResponseModel}
import dbd.core.model.existing
import dbd.core.model.existing.organization
import utopia.vault.database.Connection
import utopia.vault.model.enumeration.ComparisonOperator.Larger
import utopia.vault.nosql.access.ManyModelAccess
import utopia.vault.sql.{JoinType, Select, Where}

/**
  * Common trait for multiple invitations -access points
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
trait InvitationsAccess extends ManyModelAccess[organization.Invitation]
{
	// IMPLEMENTED	------------------------
	
	override def factory = InvitationModel
	
	
	// COMPUTED	-----------------------------
	
	/**
	  * @param connection DB Connection (implicit)
	  * @return Invitations that have been blocked
	  */
	def blocked(implicit connection: Connection) =
	{
		val additionalCondition = InvitationResponseModel.blocked.toCondition
		InvitationWithResponseFactory.getMany(mergeCondition(additionalCondition))
	}
	
	/**
	  * @param connection DB Connection (implicit)
	  * @return Invitations that are currently without response
	  */
	def pending(implicit connection: Connection) =
	{
		// Pending invitations must not be joined to a response and not be expired
		val noResponseCondition = InvitationResponseModel.table.primaryColumn.get.isNull
		val pendingCondition = InvitationModel.withExpireTime(Instant.now()).toConditionWithOperator(Larger)
		// Has to join invitation response table for the condition to work
		connection(Select(InvitationModel.target.join(InvitationResponseModel.table, JoinType.Left), InvitationModel.table) +
			Where(mergeCondition(noResponseCondition && pendingCondition))).parse(factory)
	}
}
