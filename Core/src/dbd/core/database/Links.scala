package dbd.core.database

import dbd.core.model.existing
import dbd.core.model.partial.NewLinkConfiguration
import utopia.vault.database.Connection
import utopia.vault.model.immutable.access.NonDeprecatedManyAccess

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
}
