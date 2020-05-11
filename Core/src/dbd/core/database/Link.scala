package dbd.core.database

import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.partial.NewLinkConfiguration
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{NonDeprecatedAccess, SingleIdModelAccess, SingleModelAccess}
import utopia.vault.sql.Where

/**
 * Used for accessing individual links
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
object Link extends SingleModelAccess[existing.Link] with NonDeprecatedAccess[existing.Link, Option[existing.Link]]
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
	
	class LinkById(linkId: Int) extends SingleIdModelAccess(linkId, Link.factory)
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
				Where(mergeCondition(Link.factory.notDeletedCondition))).updatedRows
		}
		
		
		// NESTED	------------------------
		
		object Configuration extends SingleModelAccess[existing.LinkConfiguration]
		{
			// IMPLEMENTED	----------------
			
			override def globalCondition = Some(factory.withLinkId(linkId).toCondition &&
				factory.nonDeprecatedCondition)
			
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
				connection(factory.nowDeprecated.toUpdateStatement() + globalCondition.map { Where(_) })
				val newConfigId = factory.forInsert(linkId, newConfig).insert().getInt
				newConfig.withId(newConfigId, linkId)
			}
		}
	}
}
