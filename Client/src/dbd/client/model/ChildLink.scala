package dbd.client.model

import dbd.core.model.existing.Link
import dbd.core.model.template.LinkLike

/**
 * Represents a link that points to a child class
 * @author Mikko Hilpinen
 * @since 23.1.2020, v0.1
 */
case class ChildLink(child: DisplayedClass, link: Link) extends LinkLike
{
	override def configuration = link.configuration
	
	/**
	 * Performs a mapping function on this link's child class
	 * @param f a mapping function
	 * @return A copy of this link with mapped child
	 */
	def mapClass(f: DisplayedClass => DisplayedClass) = copy(child = f(child))
}
