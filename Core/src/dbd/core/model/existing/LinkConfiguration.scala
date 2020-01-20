package dbd.core.model.existing

import java.time.Instant

import dbd.core.model.enumeration.LinkType
import dbd.core.model.template.LinkConfigurationLike

/**
 * Represents a DB-originated link configuration
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
case class LinkConfiguration(id: Int, linkId: Int, linkType: LinkType, originClassId: Int,
							 targetClassId: Int, isOwned: Boolean = false, deprecatedAfter: Option[Instant] = None)
	extends LinkConfigurationLike
