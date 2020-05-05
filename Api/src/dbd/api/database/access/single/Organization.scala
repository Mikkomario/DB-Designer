package dbd.api.database.access.single

import java.time.{Instant, Period}

import dbd.api.database.access.many.InvitationsAccess
import dbd.core.model.enumeration.UserRole
import dbd.core.model.partial.InvitationData
import utopia.flow.util.TimeExtensions._
import utopia.vault.database.Connection

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
		
		object Invitations extends InvitationsAccess
		{
			// IMPLEMENTED	------------------------
			
			override val globalCondition = Some(factory.withOrganizationId(organizationId).toCondition)
			
			
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
