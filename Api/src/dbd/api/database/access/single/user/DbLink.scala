package dbd.api.database.access.single.user

import dbd.api.database.model
import dbd.core.model.existing.database
import dbd.core.model.existing.database.LinkConfiguration
import dbd.core.model.partial.database.NewLinkConfiguration
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{NonDeprecatedAccess, SingleIdModelAccess, SingleModelAccess}
import utopia.vault.sql.Where

/**
 * Used for accessing individual links
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
object DbLink extends SingleModelAccess[database.Link] with NonDeprecatedAccess[database.Link, Option[database.Link]]
{
	// IMPLEMENTED	------------------------
	
	override def factory = model.database.LinkModel
	
	
	// OTHER	----------------------------
	
	/**
	 * @param id A link id
	 * @return An access point to that link's data
	 */
	def apply(id: Int) = new LinkById(id)
	
	
	// NESTED	----------------------------
	
	class LinkById(linkId: Int) extends SingleIdModelAccess(linkId, DbLink.factory)
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
			connection(DbLink.factory.nowDeleted.toUpdateStatement() +
				Where(mergeCondition(DbLink.factory.notDeletedCondition))).updatedRows
		}
		
		
		// NESTED	------------------------
		
		object Configuration extends SingleModelAccess[LinkConfiguration]
		{
			// IMPLEMENTED	----------------
			
			override def globalCondition = Some(factory.withLinkId(linkId).toCondition &&
				factory.nonDeprecatedCondition)
			
			override def factory = model.database.LinkConfigurationModel
			
			
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
