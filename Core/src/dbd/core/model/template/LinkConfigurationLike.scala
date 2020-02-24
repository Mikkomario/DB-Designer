package dbd.core.model.template

import dbd.core.model.enumeration.LinkType

/**
 * A common trait for link configuration implementations
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
trait LinkConfigurationLike
{
	// ABSTRACT	-------------------------
	
	/**
	 * @return Type of this link
	 */
	def linkType: LinkType
	/**
	 * @return Id of the class this link originates from
	 */
	def originClassId: Int
	/**
	 * @return Id of the class this link targets
	 */
	def targetClassId: Int
	/**
	 * @return Name for this link to be used in origin class context. None if not specified.
	 */
	def nameInOrigin: Option[String]
	/**
	 * @return Name for this link to be used in target class context. None if not specified.
	 */
	def nameInTarget: Option[String]
	/**
	 * @return Whether this link is owned by the origin class
	 */
	def isOwned: Boolean
	/**
	 * @return Id of the attribute in target class that specifies the uniqueness of a class relationship
	 */
	def mappingKeyAttributeId: Option[Int]
	
	
	// OTHER	------------------------
	
	/**
	 * @param other Another link configuration
	 * @return Whether these configurations are similar
	 */
	def ~==(other: LinkConfigurationLike) = linkType == other.linkType &&
		originClassId == other.originClassId && targetClassId == other.targetClassId &&
		nameInOrigin == other.nameInOrigin && nameInTarget == other.nameInTarget && isOwned == other.isOwned &&
		mappingKeyAttributeId == other.mappingKeyAttributeId
	
	/**
	  * @param classId The other class id
	  * @return The other class id in this link. None if specified class id doesn't belong to this link.
	  */
	def oppositeClassId(classId: Int) =
	{
		if (classId == originClassId)
			Some(targetClassId)
		else if (classId == targetClassId)
			Some(originClassId)
		else
			None
	}
}
