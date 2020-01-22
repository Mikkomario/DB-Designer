package dbd.client.model

import dbd.core.model.existing.Link

/**
 * Represents a link that points to a child class
 * @author Mikko Hilpinen
 * @since 23.1.2020, v0.1
 */
case class ChildLink(child: DisplayedClass, link: Link)
