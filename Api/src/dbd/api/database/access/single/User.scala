package dbd.api.database.access.single

import dbd.api.database
import dbd.api.database.access.id.UserId
import dbd.api.database.model.{UserAuth, UserDevice}
import dbd.api.util.PasswordHash
import dbd.core.model.existing
import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleIdAccess, SingleIdModelAccess, SingleModelAccess, UniqueAccess}
import utopia.vault.sql.{Select, Where}

/**
  * Used for accessing individual user's data
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object User extends SingleModelAccess[existing.User]
{
	// IMPLEMENTED	---------------------
	
	override def factory = database.model.User
	
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
	
	class SingleUser(userId: Int) extends SingleIdModelAccess[existing.User](userId, User.factory)
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
		def membershipIdInOrganizationWithId(organizationId: Int) = new MembershipId(organizationId)
		
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
		
		class MembershipId(organizationId: Int) extends SingleIdAccess[Int] with UniqueAccess[Int]
		{
			// ATTRIBUTES	------------------------
			
			private val factory = database.model.OrganizationMembership
			
			
			// IMPLEMENTED	------------------------
			
			override val condition = factory.withUserId(userId).withOrganizationId(organizationId).toCondition &&
				factory.nonDeprecatedCondition
			
			override def target = factory.target
			
			override def valueToId(value: Value) = value.int
			
			override def table = factory.table
		}
	}
}
