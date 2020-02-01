package dbd.client.database

/**
  * Accesses tables used in the client module
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
object Tables
{
	/**
	  * @return Table containing database selection recordings
	  */
	def databaseSelection = dbd.core.database.Tables("database_selection")
}
