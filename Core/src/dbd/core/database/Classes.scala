package dbd.core.database

import dbd.core.model.existing
import dbd.core.model.partial.NewClass
import utopia.vault.database.Connection
import utopia.vault.model.immutable.access.NonDeprecatedManyAccess

/**
 * Used for accessing multiple classes from DB at once
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
object Classes extends NonDeprecatedManyAccess[existing.Class]
{
	// IMPLEMENTED	--------------------
	
	override def factory = model.Class
	
	
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
		val newClassId = factory.forInsert().insert().getInt
		
		// Also inserts info and attributes
		val insertedInfo = Class(newClassId).info.update(newClass.info)
		val insertedAttributes = newClass.attributes.map { att => Class(newClassId).attributes.insert(att) }
		
		existing.Class(newClassId, insertedInfo, insertedAttributes)
	}
}
