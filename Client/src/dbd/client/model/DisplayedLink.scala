package dbd.client.model

import dbd.core.model.existing.{Class, Link}
import dbd.core.model.template.LinkLike

/**
 * A displayed version of a link
 * @author Mikko Hilpinen
 * @since 20.1.2020, v0.1
 * @param link The link to display
 * @param otherClass The other class this link currently leads to
 */
case class DisplayedLink(link: Link, otherClass: Class) extends LinkLike
{
	// IMPLEMENTED	--------------------
	
	override def configuration = link.configuration
}