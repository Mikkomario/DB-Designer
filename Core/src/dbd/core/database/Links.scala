package dbd.core.database

import dbd.core.model.existing
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
}
