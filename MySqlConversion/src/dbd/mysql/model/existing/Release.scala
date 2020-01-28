package dbd.mysql.model.existing

import java.time.Instant

import dbd.mysql.model.VersionNumber
import dbd.mysql.model.template.ReleaseLike

/**
 * Represents a recorded release
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Release(id: Int, databaseId: Int, versionNumber: VersionNumber, released: Instant) extends ReleaseLike
