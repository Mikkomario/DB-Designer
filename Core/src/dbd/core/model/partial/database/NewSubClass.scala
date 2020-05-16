package dbd.core.model.partial.database

import dbd.core.model.enumeration.LinkEndRole.{Origin, Target}
import dbd.core.model.enumeration.LinkType

/**
 * A model that contains data for adding a new sub-class
 * @author Mikko Hilpinen
 * @since 26.1.2020, v0.1
 */
case class NewSubClass(classInfo: NewClassInfo, linkType: LinkType, parentClassId: Int,
					   nameInParent: Option[String] = None, nameInChild: Option[String] = None)
{
	/**
	 * Creates a new link configuration once the child class has been created
	 * @param childClassId Id of new child class
	 * @return A new link configuration that will connect to the child class
	 */
	def toNewLinkConfiguration(childClassId: Int) =
	{
		val names = Map(linkType.fixedOwner -> nameInParent, linkType.fixedChild -> nameInChild)
		val ids = Map(linkType.fixedOwner -> parentClassId, linkType.fixedChild -> childClassId)
		NewLinkConfiguration(linkType, ids(Origin), ids(Target), names(Origin), names(Target), isOwned = true)
	}
}
