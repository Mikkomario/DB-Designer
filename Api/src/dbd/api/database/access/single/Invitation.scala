package dbd.api.database.access.single

import dbd.api.database.model.organization
import dbd.core.model.combined.InvitationWithResponse
import dbd.core.model.existing
import dbd.core.model.partial.InvitationResponseData
import dbd.core.model.post.NewInvitationResponse
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleIdModelAccess, SingleModelAccess, UniqueAccess}

/**
  * Used for accessing individual invitations
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
object Invitation extends SingleModelAccess[existing.Invitation]
{
	// IMPLEMENTED	---------------------------
	
	override def factory = organization.Invitation
	
	override def globalCondition = None
	
	
	// OTHER	-------------------------------
	
	/**
	  * @param id Invitation id
	  * @return An access point to that invitation's data
	  */
	def apply(id: Int) = new SingleInvitation(id)
	
	
	// NESTED	-------------------------------
	
	class SingleInvitation(invitationId: Int)
		extends SingleIdModelAccess[existing.Invitation](invitationId, Invitation.factory)
	{
		// COMPUTED	---------------------------
		
		/**
		  * @return An access point to this invitation's response
		  */
		def response = ResponseAccess
		
		/**
		  * @return An access point to this invitation's data where response is also included. Please note that this
		  *         access point will only find invitations that have responses.
		  */
		def withResponse = InvitationWithResponseAccess
		
		
		// NESTED	---------------------------
		
		object ResponseAccess extends SingleModelAccess[existing.InvitationResponse]
			with UniqueAccess[existing.InvitationResponse]
		{
			// IMPLEMENTED	-------------------
			
			override val condition = factory.withInvitationId(invitationId).toCondition
			
			override def factory = organization.InvitationResponse
			
			
			// OTHER	------------------------
			
			/**
			  * Inserts a new response for this invitation
			  * @param wasAccepted Whether the invitation was accepted
			  * @param wasBlocked Whether future invitations were blocked
			  * @param creatorId Invitation creator id
			  * @param connection DB Connection (implicit)
			  * @return Newly inserted response
			  */
			def insert(wasAccepted: Boolean, wasBlocked: Boolean, creatorId: Int)(implicit connection: Connection) =
			{
				factory.insert(InvitationResponseData(invitationId, wasAccepted, wasBlocked, creatorId))
			}
			
			/**
			  * Inserts a new response for this invitation
			  * @param newResponse New response
			  * @param creatorId Response creator's id
			  * @param connection DB Connection (implicit)
			  * @return Newly inserted response
			  */
			def insert(newResponse: NewInvitationResponse, creatorId: Int)
					  (implicit connection: Connection): existing.InvitationResponse =
				insert(newResponse.wasAccepted, newResponse.wasBlocked, creatorId)
		}
		
		object InvitationWithResponseAccess extends SingleIdModelAccess[InvitationWithResponse](invitationId,
			organization.InvitationWithResponse)
	}
}
