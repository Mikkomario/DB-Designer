package dbd.core.model.template

import java.time.Instant

import dbd.core.util.VersionNumber

/**
 * Common trait for DB releases
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
trait ReleaseLike
{
	/**
	 * @return Id of the updated / associated database
	 */
	def databaseId: Int
	/**
	 * @return Version number for this release
	 */
	def versionNumber: VersionNumber
	/**
	 * @return The time when this release was released
	 */
	def released: Instant
}
