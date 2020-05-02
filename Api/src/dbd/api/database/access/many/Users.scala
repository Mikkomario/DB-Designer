package dbd.api.database.access.many

import dbd.api.database
import dbd.core.model.error.{AlreadyUsedException, IllegalPostModelException}
import dbd.core.model.existing
import dbd.core.model.post.NewUser
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyModelAccess

import scala.util.Failure

/**
  * Used for accessing multiple user's data at once
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object Users extends ManyModelAccess[existing.User]
{
	// IMPLEMENTED	--------------------------
	
	override def factory = database.model.User
	
	override def globalCondition = Some(factory.nonDeprecatedCondition)
	
	
	// COMPUTED	------------------------------
	
	private def settingsFactory = database.model.UserSettings
	
	
	// OTHER	-------------------------------
	
	/**
	  * Checks whether a user name is currently in use
	  * @param userName Tested user name
	  * @param connection DB Connection (implicit)
	  * @return Whether specified user name is currently in use
	  */
	def existsUserWithName(userName: String)(implicit connection: Connection) =
	{
		settingsFactory.exists(settingsFactory.withName(userName).toCondition && settingsFactory.nonDeprecatedCondition)
	}
	
	/**
	  * Checks whether a user email is currently in use
	  * @param email Tested user email
	  * @param connection DB Connection (implicit)
	  * @return Whether specified email address is currently in use
	  */
	def existsUserWithEmail(email: String)(implicit connection: Connection) =
	{
		settingsFactory.exists(settingsFactory.withEmail(email).toCondition && settingsFactory.nonDeprecatedCondition)
	}
	
	def tryInsert(newUser: NewUser)(implicit connection: Connection) =
	{
		// Checks whether the proposed username or email already exist
		val email = newUser.settings.email.trim
		val userName = newUser.settings.name.trim
		
		if (!email.contains('@'))
			Failure(new IllegalPostModelException("Email must be a valid email address"))
		else if (userName.isEmpty)
			Failure(new IllegalPostModelException("User name must not be empty"))
		else if (existsUserWithEmail(email))
			Failure(new AlreadyUsedException("Email is already in use"))
		else if (existsUserWithName(userName))
			Failure(new AlreadyUsedException("User name is already in use"))
		else
		{
			// Inserts new user data
			???
		}
	}
}
