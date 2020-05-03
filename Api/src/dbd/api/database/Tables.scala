package dbd.api.database

/**
  * Used for accessing various tables in DB Designer project (api-side)
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object Tables
{
	// COMPUTED	--------------------------------
	
	/**
	  * @return Table that contains registered languages
	  */
	def language = apply("language")
	
	/**
	  * @return Table that contains users
	  */
	def user = apply("user")
	
	/**
	  * @return Table for user authentication
	  */
	def userAuth = apply("user_authentication")
	
	/**
	  * @return Table that contains device-specific authentication keys
	  */
	def deviceAuthKey = apply("device_authentication_key")
	
	/**
	  * @return Table that contains temporary user session keys
	  */
	def userSession = apply("user_session")
	
	/**
	  * @return Table for user settings
	  */
	def userSettings = apply("user_settings")
	
	/**
	  * @return Table that links users with languages
	  */
	def userLanguage = apply("user_language")
	
	/**
	  * @return Table that registers the devices the clients use
	  */
	def clientDevice = apply("client_device")
	
	/**
	  * @return Table that links users with the devices they are using
	  */
	def userDevice = apply("client_device_user")
	
	/**
	  * @return Table that contains descriptions of various things
	  */
	def description = apply("description")
	
	/**
	  * @return A table that contains links between devices and their descriptions
	  */
	def deviceDescription = apply("client_device_description")
	
	
	// OTHER	-------------------------------
	
	private def apply(tableName: String) = dbd.core.database.Tables(tableName)
}
