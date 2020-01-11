package dbd.core.database

import dbd.core
import utopia.vault.model.immutable.access.NonDeprecatedManyAccess

/**
 * Used for accessing multiple classes from DB at once
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
object Classes extends NonDeprecatedManyAccess[core.model.Class]
{
	override def factory = model.Class
}
