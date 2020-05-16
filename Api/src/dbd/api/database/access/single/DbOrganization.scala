package dbd.api.database.access.single

import java.time.{Instant, Period}

import dbd.api.database.model.organization
import dbd.api.database.access.many.{DbDescriptions, InvitationsAccess, OrganizationDeletionsAccess}
import dbd.api.database.model.organization.{DeletionModel, MemberRoleModel, MembershipModel}
import dbd.core.model.enumeration.UserRole
import dbd.core.model.existing.organization.Membership
import dbd.core.model.partial.organization.{DeletionData, InvitationData, MembershipData}
import utopia.flow.util.TimeExtensions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyRowModelAccess
import utopia.vault.sql.{Select, Where}

/**
  * Used for accessing individual organizations
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object DbOrganization
{
	// COMPUTED	----------------------------
	
	private def factory = organization.OrganizationModel
	
	
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
		  * @return An access point to this organization's memberships (organization-user-links)
		  */
		def memberships = Memberships
		
		/**
		  * @return An access point to invitations to join this organization
		  */
		def invitations = Invitations
		
		/**
		  * @return An access point to deletions targeting this organization
		  */
		def deletions = Deletions
		
		/**
		  * @return An access point to descriptions of this organization
		  */
		def descriptions = DbDescriptions.ofOrganizationWithId(organizationId)
		
		
		// NESTED	-------------------------------
		
		object Memberships extends ManyRowModelAccess[Membership]
		{
			// COMPUTED	---------------------------
			
			private def memberRolesFactory = MemberRoleModel
			
			
			// IMPLEMENTED	-----------------------
			
			override def factory = MembershipModel
			
			override def globalCondition = Some(factory.withOrganizationId(organizationId).toCondition &&
				factory.nonDeprecatedCondition)
			
			
			// OTHER	---------------------------
			
			/**
			  * @param role Searched user role
			  * @param connection DB Connection (implicit)
			  * @return Members within this organization that have the specified role
			  */
			def withRole(role: UserRole)(implicit connection: Connection) =
			{
				// Needs to join into role rights
				connection(Select(factory.target join memberRolesFactory.table, factory.table) +
					Where(mergeCondition(memberRolesFactory.withRole(role).toCondition))).parse(factory)
			}
			
			/**
			  * Inserts a new membership, along with a single role
			  * @param userId Id of the new organization member (user)
			  * @param startingRole Role given to the user in this organization
			  * @param creatorId Id of the user who authorized / added this membership
			  * @param connection DB Connection (implicit)
			  * @return Inserted membership
			  */
			def insert(userId: Int, startingRole: UserRole, creatorId: Int)(implicit connection: Connection) =
			{
				// Adds membership
				val newMembership = factory.insert(MembershipData(organizationId, userId, Some(creatorId)))
				// Adds user role
				memberRolesFactory.insert(newMembership.id, startingRole, creatorId)
				newMembership
			}
		}
		
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
		
		object Deletions extends OrganizationDeletionsAccess
		{
			// IMPLEMENTED	----------------------
			
			override val globalCondition = Some(DeletionModel.withOrganizationId(organizationId).toCondition)
			
			
			// OTHER	--------------------------
			
			/**
			  * Inserts a new deletion for this organization
			  * @param creatorId Id of the user starting the deletion process
			  * @param actualizationDelay Delay before actualizing this deletion
			  * @param connection DB Connection (implicit)
			  * @return Newly inserted deletion
			  */
			def insert(creatorId: Int, actualizationDelay: Period)(implicit connection: Connection) =
				DeletionModel.insert(DeletionData(organizationId, creatorId, Instant.now() + actualizationDelay))
		}
	}
}
