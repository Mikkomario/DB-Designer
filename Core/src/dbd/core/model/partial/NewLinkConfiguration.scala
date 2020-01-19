package dbd.core.model.partial

import dbd.core.model.enumeration.LinkType
import dbd.core.model.template.LinkConfigurationLike

/**
 * Represents a newly created link configuration that hasn't been saved to DB yet
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
case class NewLinkConfiguration(name: String, linkType: LinkType, originClassId: Int, targetClassId: Int,
								isOwned: Boolean = false) extends LinkConfigurationLike
