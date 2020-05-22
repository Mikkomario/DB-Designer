package dbd.core.model.partial.mysql

import java.time.Instant

import dbd.core.model.template.ReleaseLike
import dbd.core.util.VersionNumber

/**
 * Represents a release before it is stored to DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class NewRelease(databaseId: Int, versionNumber: VersionNumber, tables: Vector[NewTable]) extends ReleaseLike
{
	override def released = Instant.now()
}
