package dbd.core.database

import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.partial.NewLinkConfiguration
import utopia.vault.database.Connection
import utopia.vault.model.immutable.access.{ConditionalSingleAccess, ItemAccess, NonDeprecatedSingleAccess}
import utopia.vault.sql.Where

/**
 * Used for accessing individual links
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
object Link extends NonDeprecatedSingleAccess[existing.Link]
{
	// IMPLEMENTED	------------------------
	
	override def factory = model.Link
	
	
	// OTHER	----------------------------
	
	/**
	 * @param id A link id
	 * @return An access point to that link's data
	 */
	def apply(id: Int) = new LinkById(id)
	
	
	// NESTED	----------------------------
	
	class LinkById(linkId: Int) extends ItemAccess[existing.Link](linkId, Link.factory)
	{
		// COMPUTED	------------------------
		
		def configuration = Configuration
		
		
		// OTHER	------------------------
		
		/**
		 * Marks this link as deleted
		 * @param connection DB Connection (implicit)
		 * @return Whether a link was marked as deleted in DB (false if link doesn't exist in DB or was already deleted)
		 */
		def markDeleted()(implicit connection: Connection) =
		{
			connection(Link.factory.nowDeleted.toUpdateStatement() +
				Where(condition && Link.factory.notDeletedCondition)).updatedRows
		}
		
		
		// NESTED	------------------------
		
		object Configuration extends ConditionalSingleAccess[existing.LinkConfiguration]
		{
			// IMPLEMENTED	----------------
			
			override def condition = factory.withLinkId(linkId).toCondition && factory.nonDeprecatedCondition
			
			override def factory = model.LinkConfiguration
			
			
			// OTHER	--------------------
			
			/**
			 * Updates this link's configuration
			 * @param newConfig New configuration for the link
			 * @param connection DB Connection (implicit)
			 * @return A newly inserted configuration
			 */
			def update(newConfig: NewLinkConfiguration)(implicit connection: Connection) =
			{
				// Deletes the old version and inserts a new one
				connection(factory.nowDeprecated.toUpdateStatement() + Where(condition))
				val newConfigId = factory.forInsert(linkId, newConfig).insert().getInt
				newConfig.withId(newConfigId, linkId)
			}
		}
	}
}
