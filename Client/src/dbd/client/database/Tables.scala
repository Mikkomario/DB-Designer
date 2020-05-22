package dbd.client.database

/**
  * Accesses tables used in the client module
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
@deprecated("Tables won't be available in the client after beta", "v2")
object Tables
{
	/**
	  * @return Table containing database selection recordings
	  */
	def databaseSelection = dbd.api.database.Tables("database_selection")
}
