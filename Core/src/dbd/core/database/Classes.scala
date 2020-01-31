package dbd.core.database

import dbd.core.model.existing
import dbd.core.model.partial.NewClass
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{ManyModelAccess, NonDeprecatedAccess}

/**
 * Used for accessing multiple classes from DB at once
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
object Classes extends ManyModelAccess[existing.Class] with NonDeprecatedAccess[existing.Class, Vector[existing.Class]]
{
	// IMPLEMENTED	--------------------
	
	override def factory = model.Class
	
	
	// COMPUTED	------------------------
	
	/**
	 * @param databaseId Id of targeted database
	 * @return An access point to that database's classes
	 */
	def inDatabaseWithId(databaseId: Int) = new ClassesInDatabase(databaseId)
	
	
	// NESTED	------------------------
	
	class ClassesInDatabase(databaseId: Int) extends ManyModelAccess[existing.Class]
	{
		// IMPLEMENTED	----------------
		
		override def globalCondition = Some(Classes.this.mergeCondition(factory.withDatabaseId(databaseId).toCondition))
		
		override def factory = Classes.this.factory
		
		
		// OTHER	------------------------
		
		/**
		 * Inserts a new class to the database
		 * @param newClass A class to insert
		 * @param connection DB Connection (implicit)
		 * @return Inserted class, including generated ids
		 */
		def insert(newClass: NewClass)(implicit connection: Connection) =
		{
			// Inserts the class portion first
			val newClassId = factory.forInsert(databaseId).insert().getInt
			
			// Also inserts info and attributes
			val insertedInfo = Class(newClassId).info.update(newClass.info)
			val insertedAttributes = newClass.attributes.map { att => Class(newClassId).attributes.insert(att) }
			
			existing.Class(newClassId, databaseId, insertedInfo, insertedAttributes)
		}
	}
}
