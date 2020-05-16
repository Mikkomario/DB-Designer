package dbd.api.database.access.many

import dbd.api.database.access.single.{DbDevice, DbLanguage}
import dbd.api.database.model.user.{UserModel, UserDeviceModel, UserLanguageModel, UserSettingsModel}
import dbd.core.model.combined.user.UserWithLinks
import dbd.core.model.error.{AlreadyUsedException, IllegalPostModelException}
import dbd.core.model.existing.user
import dbd.core.model.{combined, existing}
import dbd.core.model.post.NewUser
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyModelAccess

import scala.util.{Failure, Success, Try}

/**
  * Used for accessing multiple user's data at once
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object DbUsers extends ManyModelAccess[user.User]
{
	// IMPLEMENTED	--------------------------
	
	override def factory = UserModel
	
	override def globalCondition = Some(factory.nonDeprecatedCondition)
	
	
	// COMPUTED	------------------------------
	
	private def settingsFactory = UserSettingsModel
	
	
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
	
	/**
	  * Inserts a new user to the DB
	  * @param newUser New user data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted data. Failure with IllegalPostModelException if posted data was invalid. Failure with
	  *         AlreadyUsedException if user name or email was already in use.
	  */
	def tryInsert(newUser: NewUser)(implicit connection: Connection): Try[UserWithLinks] =
	{
		// Checks whether the proposed email already exist
		val email = newUser.settings.email.trim
		val userName = newUser.settings.name.trim
		
		if (!email.contains('@'))
			Failure(new IllegalPostModelException("Email must be a valid email address"))
		else if (userName.isEmpty)
			Failure(new IllegalPostModelException("User name must not be empty"))
		else if (existsUserWithEmail(email))
			Failure(new AlreadyUsedException("Email is already in use"))
		else
		{
			// Makes sure provided device id or language id matches data in the DB
			val idsAreValid = newUser.device match
			{
				case Right(deviceId) => DbDevice(deviceId).isDefined
				case Left(nameAndLanguage) => DbLanguage(nameAndLanguage._2).isDefined
			}
			
			if (idsAreValid)
			{
				// Inserts new user data
				val user = factory.insert(newUser.settings, newUser.password)
				newUser.languageIds.foreach { languageId => UserLanguageModel.insert(user.id, languageId) }
				// Links user with device (uses existing or a new device)
				val deviceId = newUser.device match
				{
					case Right(deviceId) => deviceId
					case Left(deviceData) => DbDevices.insert(deviceData._1, deviceData._2, user.id).deviceId
					
				}
				UserDeviceModel.insert(user.id, deviceId)
				// Returns inserted user
				Success(combined.user.UserWithLinks(user, newUser.languageIds, Vector(deviceId)))
			}
			else
				Failure(new IllegalPostModelException("device_id and language_id must point to existing data"))
		}
	}
}
