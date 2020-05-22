package dbd.api.database.access.many.database

import dbd.api.database.access.id.ClassIds
import dbd.api.database.access.many.description.ChangedModelsAccess
import dbd.api.database.access.single.database.DbClass
import dbd.api.database.model
import dbd.core.model.existing.database
import dbd.core.model.existing.database.ClassInfo
import dbd.core.model.partial.database.NewClass
import utopia.vault.database.Connection
import utopia.vault.nosql.access.NonDeprecatedAccess

/**
 * Used for accessing multiple classes from DB at once
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
object DbClasses extends ChangedModelsAccess[database.Class, ClassInfo] with NonDeprecatedAccess[database.Class, Vector[database.Class]]
{
	// IMPLEMENTED	--------------------
	
	override def creationTimeColumn = factory.creationTimeColumn
	
	override def configurationFactory = model.database.ClassInfoModel
	
	override def factory = model.database.ClassModel
	
	
	// COMPUTED	------------------------
	
	/**
	  * @return An access point to class ids (without class data included)
	  */
	def ids = ClassIds
	
	/**
	  * @return An access point to deleted classes
	  */
	def deleted = DbDeletedClasses
	
	
	// OTHER	------------------------
	
	/**
	  * @param databaseId Id of targeted database
	  * @return An access point to that database's classes
	  */
	def inDatabaseWithId(databaseId: Int) = new ClassesInDatabase(databaseId)
	
	
	// NESTED	------------------------
	
	class ClassesInDatabase(databaseId: Int) extends ChangedModelsAccess[database.Class, ClassInfo]
	{
		// IMPLEMENTED	----------------
		
		override def creationTimeColumn = factory.creationTimeColumn
		
		override def configurationFactory = DbClasses.this.configurationFactory
		
		override def globalCondition = Some(DbClasses.this.mergeCondition(factory.withDatabaseId(databaseId).toCondition))
		
		override def factory = DbClasses.this.factory
		
		
		// COMPUTED	------------------------
		
		/**
		  * @return An access point to deleted classes in this db
		  */
		def deleted = DbClasses.this.deleted.inDatabaseWithId(databaseId)
		
		
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
			val insertedInfo = DbClass(newClassId).info.update(newClass.info)
			val insertedAttributes = newClass.attributes.map { att => DbClass(newClassId).attributes.insert(att) }
			
			database.Class(newClassId, databaseId, insertedInfo, insertedAttributes)
		}
	}
}
