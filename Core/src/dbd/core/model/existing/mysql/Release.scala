package dbd.core.model.existing.mysql

import java.time.Instant

import dbd.core.model.template.ReleaseLike
import dbd.core.util.VersionNumber

/**
 * Represents a recorded release
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Release(id: Int, databaseId: Int, versionNumber: VersionNumber, released: Instant) extends ReleaseLike
