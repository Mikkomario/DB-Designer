package dbd.core.database

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.partial.NewLinkConfiguration
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{ManyModelAccess, NonDeprecatedAccess}
import utopia.vault.sql.{Update, Where}

/**
 * Used for accessing multiple non-deprecated links at a time
 * @author Mikko HIlpinen
 * @since 19.1.2020, v0.1
 */
object Links extends ManyModelAccess[existing.Link] with NonDeprecatedAccess[existing.Link, Vector[existing.Link]]
{
	// IMPLEMENTED	----------------------
	
	override def factory = model.Link
	
	
	// OTHER	--------------------------
	
	/**
	 * @param databaseId Id of targeted database
	 * @return An access point to that database's links
	 */
	def inDatabaseWithId(databaseId: Int) = new LinksInDatabase(databaseId)
	
	/**
	 * @param classId Id of target class
	 * @return An access point to all links attached to that class
	 */
	def attachedToClassWithId(classId: Int) = new ClassAttachedLinks(classId)
	
	/**
	 * @param attributeId Id of target attribute
	 * @return An access point to all links that use the specified attribute as a mapping key
	 */
	def usingAttributeWithId(attributeId: Int) = new AttributeUsingLinks(attributeId)
	
	
	// NESTED	--------------------------
	
	/**
	 * Used for accessing links under a specific database
	 * @param databaseId Id of target database
	 */
	class LinksInDatabase(databaseId: Int) extends ChangedModelsAccess[existing.Link, existing.LinkConfiguration]
	{
		// IMPLEMENTED	------------------
		
		override def creationTimeColumn = factory.creationTimeColumn
		
		override def configurationFactory = model.LinkConfiguration
		
		override def globalCondition = Some(Links.this.mergeCondition(factory.withDatabaseId(databaseId).toCondition))
		
		override def factory = Links.this.factory
		
		
		// OTHER	----------------------
		
		/**
		 * Inserts a new link to the database
		 * @param newConfig Initial configuration for the new link
		 * @param connection DB Connection (implicit)
		 * @return The newly inserted link
		 */
		def insert(newConfig: NewLinkConfiguration)(implicit connection: Connection) =
		{
			// Inserts a new link
			val newLinkId = factory.forInsert(databaseId).insert().getInt
			// Adds a configuration for that link
			val insertedConfig = Link(newLinkId).configuration.update(newConfig)
			
			existing.Link(newLinkId, databaseId, insertedConfig)
		}
	}
	
	/**
	 * Access point to links attached to a class (as origin or target)
	 * @param classId Id of target class
	 */
	class ClassAttachedLinks(classId: Int) extends ManyModelAccess[existing.Link]
	{
		// IMPLEMENTED	------------------
		
		override def globalCondition = Some(Links.this.mergeCondition(
			model.LinkConfiguration.classConnectColumns.values.map { _ <=> classId }.reduce { _ || _ }))
		
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
				Where(mergeCondition(factory.notDeletedCondition))).updatedRowCount
		}
	}
	
	/**
	 * Access points to links using a specified attribute
	 * @param attributeId Id of used attribute
	 */
	class AttributeUsingLinks(attributeId: Int) extends ManyModelAccess[existing.Link]
	{
		// IMPLEMENTED	-----------------
		
		override def globalCondition = Some(Links.this.mergeCondition(
			model.LinkConfiguration.withMappingKeyAttributeId(attributeId).toCondition))
		
		override def factory = Links.this.factory
		
		
		// OTHER	---------------------
		
		/**
		 * Marks all of these links as deleted
		 * @param connection DB Connection (implicit)
		 * @return The number of links that were marked as deleted
		 */
		def markDeleted()(implicit connection: Connection) =
		{
			connection(Update.apply(factory.target, factory.table, factory.deletedAfterVarName, Instant.now()) +
				Where(mergeCondition(factory.notDeletedCondition))).updatedRowCount
		}
	}
}
