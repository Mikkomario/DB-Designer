package dbd.core.model.partial.database

import dbd.core.model.enumeration.LinkType
import dbd.core.model.existing.database
import dbd.core.model.existing.database.LinkConfiguration
import dbd.core.model.template.LinkConfigurationLike

/**
 * Represents a newly created link configuration that hasn't been saved to DB yet
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
case class NewLinkConfiguration(linkType: LinkType, originClassId: Int, targetClassId: Int,
								nameInOrigin: Option[String] = None, nameInTarget: Option[String] = None,
								isOwned: Boolean = false, mappingKeyAttributeId: Option[Int] = None)
	extends LinkConfigurationLike
{
	/**
	 * @param id A new id for this model
	 * @param linkId The id of the described link
	 * @return A new model with id data included
	 */
	def withId(id: Int, linkId: Int) = database.LinkConfiguration(id, linkId, linkType, originClassId, targetClassId,
		nameInOrigin, nameInTarget, isOwned, mappingKeyAttributeId)
}
