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
	// COMPUTED	------------------------
	
	/**
	 * @return The name that should be displayed on this link
	 */
	def displayName =
	{
		{
			if (originClassId == otherClass.id)
				configuration.nameInTarget
			else if (targetClassId == otherClass.id)
				configuration.nameInOrigin
			else
				None
		}.getOrElse(otherClass.name)
	}
	
	
	// IMPLEMENTED	--------------------
	
	override def configuration = link.configuration
	
	override def toString = displayName
}