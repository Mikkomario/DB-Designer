package dbd.core.database

import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import utopia.vault.model.immutable.access.{ItemAccess, NonDeprecatedSingleAccess}

/**
 * An access point for individual databases
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object Database extends NonDeprecatedSingleAccess[existing.Database]
{
	// IMPLEMENTED	-------------------
	
	override def factory = model.Database
	
	
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
	class SingleDatabase(databaseId: Int) extends ItemAccess[existing.Database](databaseId, Database.this.factory)
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
