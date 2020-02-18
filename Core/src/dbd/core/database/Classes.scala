package dbd.core.database

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import utopia.vault.sql.Extensions._
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
	
	/**
	  * @return An access point to class ids (without class data included)
	  */
	def ids = ClassIds
	
	
	// OTHER	------------------------
	
	// TODO: These methods should apply under specific database condition. Also, add similar methods to attributes and links
	
	/**
	  * @param threshold A time threshold
	  * @param connection DB Connection
	  * @return All new, non-deleted classes that were created after the specified time threshold
	  */
	def createdAfter(threshold: Instant)(implicit connection: Connection) =
		find(factory.creationTimeColumn > threshold)
	
	/**
	  * @param threshold A time threshold
	  * @param connection DB Connection
	  * @return All non-deleted classes that were created before the specified instant but were modified after that
	  */
	def modifiedAfter(threshold: Instant)(implicit connection: Connection) =
		find(model.ClassInfo.createdAfterCondition(threshold) && factory.creationTimeColumn <= threshold)
	
	
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
