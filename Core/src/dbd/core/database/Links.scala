package dbd.core.database

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.partial.NewLinkConfiguration
import utopia.vault.database.Connection
import utopia.vault.model.immutable.access.{ConditionalManyAccess, NonDeprecatedManyAccess}
import utopia.vault.sql.{Update, Where}

/**
 * Used for accessing multiple non-deprecated links at a time
 * @author Mikko HIlpinen
 * @since 19.1.2020, v0.1
 */
object Links extends NonDeprecatedManyAccess[existing.Link]
{
	// IMPLEMENTED	----------------------
	
	override def factory = model.Link
	
	
	// OTHER	--------------------------
	
	/**
	 * @param classId Id of target class
	 * @return An access point to all links attached to that class
	 */
	def attachedToClassWithId(classId: Int) = new AttachedLinks(classId)
	
	/**
	 * Inserts a new link to the database
	 * @param newConfig Initial configuration for the new link
	 * @param connection DB Connection (implicit)
	 * @return The newly inserted link
	 */
	def insert(newConfig: NewLinkConfiguration)(implicit connection: Connection) =
	{
		// Inserts a new link
		val newLinkId = factory.forInsert().insert().getInt
		// Adds a configuration for that link
		val insertedConfig = Link(newLinkId).configuration.update(newConfig)
		
		existing.Link(newLinkId, insertedConfig)
	}
	
	
	// NESTED	--------------------------
	
	/**
	 * Access point to links attached to a class (as origin or target)
	 * @param classId Id of target class
	 */
	class AttachedLinks(classId: Int) extends ConditionalManyAccess[existing.Link]
	{
		// IMPLEMENTED	------------------
		
		override def condition = Links.this.condition &&
			model.LinkConfiguration.classConnectColumns.values.map { _ <=> classId }.reduce { _ || _ }
		
		override def factory = Links.this.factory
		
		
		// OTHER	----------------------
		
		/**
		 * Marks all of these links as deleted
		 * @param connection DB Connection (implicit)
		 * @return How many links were marked as deleted
		 */
		def markDeleted()(implicit connection: Connection) =
		{
			connection(Update.apply(factory.target, factory.table, factory.deletedAfterVarName, Instant.now()) +
				Where(condition && factory.notDeletedCondition)).updatedRowCount
		}
	}
}
