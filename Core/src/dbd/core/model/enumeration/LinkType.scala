package dbd.core.model.enumeration

import dbd.core.model.enumeration.LinkTypeCategory.{ManyToMany, OneToMany, OneToOne}

/**
 * A common trait for various link types
 * @author Mikko Hilpinen
 * @since 18.1.2020, v0.1
 */
sealed trait LinkType
{
	// ABSTRACT	--------------------
	
	/**
	 * @return A unique identifier for this link type
	 */
	def id: Int
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
	/**
	 * A common trait for links that have one origin and multiple target items
	 */
	sealed trait OneToManyType extends LinkType
	{
		override def category = OneToMany
		override def isOwnable = true
		override def isFixedLinkOrigin = true
	}
	
	/**
	 * A common trait for links that have a single origin and target item
	 */
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
		override def id = 1
		override def usesDeprecation = false
	}
	/**
	 * This link type allows one to link multiple targets to one origin. The targets support deprecation.
	 */
	case object DeprecatingOneToMany extends OneToManyType
	{
		override def id = 2
		override def usesDeprecation = true
	}
	/**
	 * This link type allows one to link a single active item per key to one origin. Previous map values will be
	 * deprecated as new ones are added.
	 */
	case object DeprecatingMap extends OneToManyType
	{
		override def id = 3
		override def usesDeprecation = true
	}
	/**
	 * This link type allows one to link multiple items to a single link origin. The items must be unique by a
	 * search key, however
	 */
	case object EnforcedMap extends OneToManyType
	{
		override def id = 4
		override def usesDeprecation = false
	}
	
	/**
	 * This link type allows one to link a single target item to a single origin item.
	 */
	case object EnforcedOneToOne extends OneToOneType
	{
		override def id = 5
		override def isFixedLinkOrigin = false
		override def usesDeprecation = false
	}
	/**
	 * This link type allows one to link a changing target item to a single origin item. Whenever a new target item
	 * is added, the previous is deprecated.
	 */
	case object DeprecatingOneToOne extends OneToOneType
	{
		override def id = 6
		override def isFixedLinkOrigin = true
		override def usesDeprecation = true
	}
	
	/**
	 * This link type allows one to link multiple origin items with multiple target items
	 */
	case object BasicManyToMany extends LinkType
	{
		override def id = 7
		override def category = ManyToMany
		override def isOwnable = false
		override def isFixedLinkOrigin = true
		override def usesDeprecation = false
	}
	
	
	// ATTRIBUTES	-------------------------
	
	/**
	 * All currently known link types
	 */
	val values: Vector[LinkType] = Vector(BasicOneToMany, DeprecatingOneToMany, DeprecatingMap, EnforcedMap,
		EnforcedOneToOne, DeprecatingOneToOne, BasicManyToMany)
	
	/**
	 * @param id Link type id
	 * @return A link type matching specified id
	 */
	def forId(id: Int) = values.find { _.id == id }
}