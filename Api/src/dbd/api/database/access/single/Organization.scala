package dbd.api.database.access.single

import java.time.{Instant, Period}

import dbd.api.database.model.{Invitation, InvitationResponse, InvitationWithResponse}
import dbd.core.model.enumeration.UserRole
import dbd.core.model.existing
import dbd.core.model.partial.InvitationData
import utopia.flow.util.TimeExtensions._
import utopia.vault.database.Connection
import utopia.vault.model.enumeration.ComparisonOperator.Larger
import utopia.vault.nosql.access.ManyModelAccess
import utopia.vault.sql.{JoinType, Select, Where}

/**
  * Used for accessing individual organizations
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object Organization
{
	// OTHER	----------------------------
	
	/**
	  * @param id An organization id
	  * @return An access point to that organization's data
	  */
	def apply(id: Int) = new SingleOrganization(id)
	
	
	// NESTED	----------------------------
	
	class SingleOrganization(organizationId: Int)
	{
		// COMPUTED	------------------------------
		
		/**
		  * @return An access point to invitations to join this organization
		  */
		def invitations = Invitations
		
		
		// NESTED	-------------------------------
		
		object Invitations extends ManyModelAccess[existing.Invitation]
		{
			// IMPLEMENTED	------------------------
			
			override def factory = Invitation
			
			override val globalCondition = Some(factory.withOrganizationId(organizationId).toCondition)
			
			
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
			
			
			// OTHER	---------------------------
			
			/**
			  * Sends a new invitation. Please make sure the user is allowed to send this invitation before calling this
			  * method.
			  * @param recipient Either recipient user id (right) or recipient user email (left)
			  * @param role The role the user will receive upon accepting this invitation
			  * @param senderId Id of the user sending this invitation
			  * @param validDuration Duration how long the invitation can still be answered (default = 1 week)
			  * @param connection DB Connection (implicit)
			  * @return Newly saved invitation
			  */
			def send(recipient: Either[String, Int], role: UserRole, senderId: Int, validDuration: Period = 7.days)
					(implicit connection: Connection) =
			{
				factory.insert(InvitationData(organizationId, recipient, role, Instant.now() + validDuration, Some(senderId)))
			}
		}
	}
}
