package dbd.core.model.template

import dbd.core.model.enumeration.LinkType

/**
 * A common trait for link configuration implementations
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
trait LinkConfigurationLike
{
	/**
	 * @return Name of this link
	 */
	def name: String
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
	 * @return Whether this link is owned by the origin class
	 */
	def isOwned: Boolean
}
