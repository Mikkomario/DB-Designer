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
	  * @return Table for user authentication
	  */
	def userAuth = apply("user_authentication")
	
	/**
	  * @return Table for user settings
	  */
	def userSettings = apply("user_settings")
	
	
	// OTHER	-------------------------------
	
	private def apply(tableName: String) = dbd.core.database.Tables(tableName)
}
