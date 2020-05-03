package dbd.api.database.access.single

import dbd.api.database
import dbd.api.database.access.id.UserId
import dbd.api.database.model.UserAuth
import dbd.api.util.PasswordHash
import dbd.core.model.existing
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleIdModelAccess, SingleModelAccess}
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
		/**
		  * @param connection DB Connection (implicit)
		  * @return Password hash for this user. None if no hash was found.
		  */
		def passwordHash(implicit connection: Connection) =
		{
			connection(Select(UserAuth.table, UserAuth.hashAttName) + Where(UserAuth.withUserId(userId).toCondition))
				.firstValue.string
		}
	}
}
