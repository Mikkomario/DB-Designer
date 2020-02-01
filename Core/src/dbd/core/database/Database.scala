package dbd.core.database

import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{NonDeprecatedAccess, SingleIdModelAccess, SingleModelAccess}

/**
 * An access point for individual databases
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object Database extends SingleModelAccess[existing.Database] with NonDeprecatedAccess[existing.Database, Option[existing.Database]]
{
	// IMPLEMENTED	-------------------
	
	override def factory = model.Database
	
	
	// COMPUTED	-----------------------
	
	/**
	  * @param connection Implicit DB connection
	  * @return The last configured database from the DB
	  */
	def lastConfigured(implicit connection: Connection) = first(model.DatabaseConfiguration.defaultOrdering)
	
	
	// OTHER	-----------------------
	
	/**
	 * @param id Id of targeted database
	 * @return An access point to that database's data
	 */
	def apply(id: Int) = new SingleDatabase(id)
	
	
	// NESTED	-----------------------
	
	/**
	 * Used for accessing a single database's data
	 * @param databaseId Id of targeted database
	 */
	class SingleDatabase(databaseId: Int) extends SingleIdModelAccess[existing.Database](databaseId, Database.this.factory)
	{
		/**
		 * @return An access point to this database's classes
		 */
		def classes = Classes.inDatabaseWithId(databaseId)
		
		/**
		 * @return An access point to this database's links
		 */
		def links = Links.inDatabaseWithId(databaseId)
	}
}
