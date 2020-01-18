package dbd.core.model

import dbd.core.model.LinkTypeCategory.{ManyToMany, OneToMany, OneToOne}

/**
 * A common trait for various link types
 * @author Mikko Hilpinen
 * @since 18.1.2020, v0.1
 */
sealed trait LinkType
{
	// ABSTRACT	--------------------
	
	/**
	 * @return The category this link type belongs to
	 */
	def category: LinkTypeCategory
	/**
	 * @return Whether this link type supports ownership
	 */
	def isOwnable: Boolean
	/**
	 * @return Whether this link type always has a fixed link origin
	 */
	def isFixedLinkOrigin: Boolean
	/**
	 * @return Whether this link type utilizes deprecation of some description
	 */
	def usesDeprecation: Boolean
	
	
	// COMPUTED	--------------------
	
	/**
	 * @return Whether this link type allows one to place multiple links for a single target
	 */
	def allowsMultipleOrigins = category.allowsMultipleOrigins
	/**
	 * @return Whether this link type allows one to place multiple links for a single origin
	 */
	def allowsMultipleTargets = category.allowsMultipleTargets
}

object LinkType
{
	sealed trait OneToManyType extends LinkType
	{
		override def category = OneToMany
		override def isOwnable = true
		override def isFixedLinkOrigin = true
	}
	
	sealed trait OneToOneType extends LinkType
	{
		override def category = OneToOne
		override def isOwnable = true
	}
	
	/**
	 * This link type allows one to link multiple targets to one origin without any added features
	 */
	case object BasicOneToMany extends OneToManyType
	{
		override def usesDeprecation = false
	}
	/**
	 * This link type allows one to link multiple targets to one origin. The targets support deprecation.
	 */
	case object DeprecatingOneToMany extends OneToManyType
	{
		override def usesDeprecation = true
	}
	/**
	 * This link type allows one to link a single active item per key to one origin. Previous map values will be
	 * deprecated as new ones are added.
	 */
	case object DeprecatingMap extends OneToManyType
	{
		override def usesDeprecation = true
	}
	case object EnforcedMap extends OneToManyType
	{
		override def usesDeprecation = false
	}
	
	case object EnforcedOneToOne extends OneToOneType
	{
		override def isFixedLinkOrigin = false
		override def usesDeprecation = false
	}
	case object DeprecatingOneToOne extends OneToOneType
	{
		override def isFixedLinkOrigin = true
		override def usesDeprecation = true
	}
	
	case object BasicManyToMany extends LinkType
	{
		override def category = ManyToMany
		override def isOwnable = false
		override def isFixedLinkOrigin = true
		override def usesDeprecation = false
	}
}