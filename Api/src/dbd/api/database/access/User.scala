package dbd.api.database.access

import dbd.api.database.access.id.UserId
import dbd.api.database.model.UserAuth
import dbd.api.util.PasswordHash
import utopia.vault.database.Connection
import utopia.vault.sql.{Select, Where}

/**
  * Used for accessing individual user's data
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object User
{
	// OTHER	-------------------------
	
	/**
	  * @param userId Id of targeted user
	  * @return An access point to that user's data
	  */
	def apply(userId: Int) = new SingleUser(userId)
	
	/**
	  * Tries to authenticate a user with user name (or email) + password
	  * @param nameOrEmail User name or user email
	  * @param password Password
	  * @param connection Database connection
	  * @return User id if authenticated, None otherwise.
	  */
	def tryAuthenticate(nameOrEmail: String, password: String)(implicit connection: Connection) =
	{
		// Finds user id
		val userId = if (nameOrEmail.contains("@")) UserId.forEmail(nameOrEmail) else UserId.forName(nameOrEmail)
		// Checks the password
		userId.filter { id =>
			apply(id).passwordHash.exists { correctHash => PasswordHash.validatePassword(password, correctHash) }
		}
	}
	
	
	// NESTED	-------------------------
	
	class SingleUser(userId: Int)
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
