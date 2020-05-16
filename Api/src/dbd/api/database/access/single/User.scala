package dbd.api.database.access.single

import dbd.api.database.model.user
import dbd.api.database.access.id.UserId
import dbd.api.database.access.many.InvitationsAccess
import dbd.api.database.model.description.OrganizationDescription
import dbd.api.database.model.organization.{MembershipWithRoles, OrganizationMembership, RoleRight}
import dbd.api.database.model.user.{UserAuth, UserDevice, UserSettings}
import dbd.api.util.PasswordHash
import dbd.core.model.combined.{MyOrganization, RoleWithRights}
import dbd.core.model.existing
import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.enumeration.BasicCombineOperator.Or
import utopia.vault.nosql.access.{ManyModelAccess, SingleIdAccess, SingleIdModelAccess, SingleModelAccess, UniqueAccess}
import utopia.vault.sql.{Select, Where}
import utopia.vault.sql.Extensions._

/**
  * Used for accessing individual user's data
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object User extends SingleModelAccess[existing.User]
{
	// IMPLEMENTED	---------------------
	
	override def factory = user.User
	
	override def globalCondition = Some(factory.nonDeprecatedCondition)
	
	
	// OTHER	-------------------------
	
	/**
	  * @param userId Id of targeted user
	  * @return An access point to that user's data
	  */
	def apply(userId: Int) = new SingleUser(userId)
	
	/**
	  * Tries to authenticate a user with user name (or email) + password
	  * @param email User email
	  * @param password Password
	  * @param connection Database connection
	  * @return User id if authenticated, None otherwise.
	  */
	def tryAuthenticate(email: String, password: String)(implicit connection: Connection) =
	{
		// Finds user id and checks the password
		UserId.forEmail(email).filter { id =>
			apply(id).passwordHash.exists { correctHash => PasswordHash.validatePassword(password, correctHash) }
		}
	}
	
	
	// NESTED	-------------------------
	
	class SingleUser(userId: Int) extends SingleIdModelAccess(userId, User.factory)
	{
		// COMPUTED	---------------------
		
		/**
		  * @param connection DB Connection (implicit)
		  * @return Password hash for this user. None if no hash was found.
		  */
		def passwordHash(implicit connection: Connection) =
		{
			connection(Select(UserAuth.table, UserAuth.hashAttName) + Where(UserAuth.withUserId(userId).toCondition))
				.firstValue.string
		}
		
		/**
		  * @param connection DB Connection (implicit)
		  * @return Current settings for this user
		  */
		def settings(implicit connection: Connection) =
		{
			val settingsFactory = UserSettings
			settingsFactory.get(settingsFactory.withUserId(userId).toCondition && settingsFactory.nonDeprecatedCondition)
		}
		
		
		/**
		  * @param connection DB Connection (implicit), used for reading user email address
		  * @return An access point to invitations for this user
		  */
		// Will need to read settings for accessing since joining logic would get rather complex otherwise
		def receivedInvitations(implicit connection: Connection) = new InvitationsForUser(settings.map { _.email })
		
		/**
		  * @return An access point to this user's memberships
		  */
		def memberships = Memberships
		
		
		// OTHER	----------------------
		
		/**
		  * @param organizationId Id of the targeted organization
		  * @param connection DB Connection (implicit)
		  * @return Whether this user is a member of the specified organization
		  */
		def isMemberInOrganizationWithId(organizationId: Int)(implicit connection: Connection) =
			membershipIdInOrganizationWithId(organizationId).isDefined
		
		/**
		  * @param organizationId Id of targeted organization
		  * @return An access point to this user's membership id in that organization
		  */
		def membershipIdInOrganizationWithId(organizationId: Int) = MembershipId(organizationId)
		
		/**
		  * Links this user with the specified device
		  * @param deviceId Id of targeted device (must be valid)
		  * @param connection DB Connection (implicit)
		  * @return Whether a new link was created (false if there already existed a link between this user and the device)
		  */
		def linkWithDeviceWithId(deviceId: Int)(implicit connection: Connection) =
		{
			// Checks whether there already exists a connection between this user and specified device
			if (UserDevice.exists(UserDevice.withUserId(userId).withDeviceId(deviceId).toCondition &&
				UserDevice.nonDeprecatedCondition))
				false
			else
			{
				UserDevice.insert(userId, deviceId)
				true
			}
		}
		
		
		// NESTED	-----------------------
		
		case class MembershipId(organizationId: Int) extends SingleIdAccess[Int] with UniqueAccess[Int]
		{
			// ATTRIBUTES	------------------------
			
			private val factory = OrganizationMembership
			
			
			// IMPLEMENTED	------------------------
			
			override val condition = factory.withUserId(userId).withOrganizationId(organizationId).toCondition &&
				factory.nonDeprecatedCondition
			
			override def target = factory.target
			
			override def valueToId(value: Value) = value.int
			
			override def table = factory.table
		}
		
		// If email is empty, it is not searched
		class InvitationsForUser(email: Option[String]) extends InvitationsAccess
		{
			override val globalCondition =
			{
				email match
				{
					case Some(email) => Some(factory.withRecipientId(userId)
						.withRecipientEmail(email).toConditionWithOperator(combineOperator = Or))
					case None => Some(factory.withRecipientId(userId).toCondition)
				}
			}
		}
		
		object Memberships extends ManyModelAccess[existing.Membership]
		{
			// IMPLEMENTED	---------------------------
			
			override def factory = OrganizationMembership
			
			override def globalCondition = Some(userCondition && factory.nonDeprecatedCondition)
			
			
			// COMPUTED	--------------------------------
			
			private def userCondition = factory.withUserId(userId).toCondition
			
			/**
			  * All organizations & roles associated with these memberships
			  * @param connection DB Connection (implicit)
			  * @return A list of organizations, along with all roles, rights and descriptions that these
			  *         memberships link to
			  */
			def myOrganizations(implicit connection: Connection) =
			{
				// Reads all memberships & roles first
				val memberships = MembershipWithRoles.getMany(userCondition && MembershipWithRoles.nonDeprecatedCondition)
				// Reads organization descriptions
				val organizationIds = memberships.map { _.wrapped.organizationId }.toSet
				if (organizationIds.nonEmpty)
				{
					val organizationDescriptions = OrganizationDescription.getMany(
						OrganizationDescription.targetIdColumn.in(organizationIds))
					// Reads all task links
					val roleIds = memberships.flatMap { _.roles }.map { _.id }.toSet
					val roleRights = RoleRight.getMany(RoleRight.roleIdColumn.in(roleIds))
					// Groups the information
					val rolesWithRights = roleRights.groupBy { _.role }.map { case (role, links) =>
						RoleWithRights(role, links.map { _.task }.toSet) }
					
					memberships.map { membership =>
						val organizationId = membership.wrapped.organizationId
						MyOrganization(organizationId, userId,
							organizationDescriptions.filter { _.organizationId == organizationId }.toSet,
							membership.roles.flatMap { role => rolesWithRights.find { _.role == role } })
					}
				}
				else
					Vector()
			}
		}
	}
}
