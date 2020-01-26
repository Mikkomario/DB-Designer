package dbd.core.model.template

import dbd.core.model.enumeration.LinkEndRole
import dbd.core.model.enumeration.LinkEndRole.{Origin, Target}

/**
 * A common trait for link implementations
 * @author Mikko
 * @since 19.1.2020, v
 */
trait LinkLike
{
	// ABSTRACT	----------------------
	
	/**
	 * @return The current configuration of this link
	 */
	def configuration: LinkConfigurationLike
	
	
	// COMPUTED	----------------------
	
	/**
	 * @return The current type for this link
	 */
	def linkType = configuration.linkType
	
	/**
	 * @return Id of class this link targets
	 */
	def targetClassId = configuration.targetClassId
	
	/**
	 * @return Id of class this link originates from
	 */
	def originClassId = configuration.originClassId
	
	/**
	 * @return Whether this link represents a parent-child (nested) relationship
	 */
	def isOwned = configuration.isOwned
	
	/**
	 * @return The ids of classes that are part of this link
	 */
	def classIds = Set(originClassId, targetClassId)
	
	/**
	 * @return The id of the class that owns this link / acts as the parent class
	 */
	def ownerClassId = if (isOwned) Some(classIdForRole(linkType.fixedOwner)) else None
	
	/**
	 * @return The id of the child class under this link. None if this link doesn't have containment / ownership.
	 */
	def childClassId = if (isOwned) Some(classIdForRole(linkType.fixedOwner.opposite)) else None
	
	/**
	 * @return Name of this link in owner class
	 */
	def nameInOwner = nameInRole(linkType.fixedOwner)
	
	/**
	 * @return Name of this link in child class
	 */
	def nameInChild = nameInRole(linkType.fixedChild)
	
	/**
	 * @return Id of the attribute that is used as a map key (None if no mapping is used)
	 */
	def mappingKeyAttributeId = configuration.mappingKeyAttributeId
	
	
	// OTHER	------------------------
	
	/**
	 * @param role A link end role
	 * @return The class id for that role
	 */
	def classIdForRole(role: LinkEndRole) = role match
	{
		case Origin => originClassId
		case Target => targetClassId
	}
	
	/**
	 * @param role A link end role
	 * @return Specified link name for that role (None if no link name is specified for that role)
	 */
	def nameInRole(role: LinkEndRole) = role match
	{
		case Origin => configuration.nameInOrigin
		case Target => configuration.nameInTarget
	}
}
