package dbd.core.model.existing

import java.time.Instant

import dbd.core.model.template.LinkLike

/**
 * Represents a DB-originated link between two classes
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
case class Link(id: Int, configuration: LinkConfiguration, deletedAfter: Option[Instant] = None) extends LinkLike
