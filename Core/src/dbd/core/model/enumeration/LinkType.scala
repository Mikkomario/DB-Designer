package dbd.core.model.enumeration

import dbd.core.model.enumeration.LinkEndRole.{Origin, Target}
import dbd.core.model.enumeration.LinkTypeCategory.{ManyToMany, ManyToOne, OneToOne}

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
	/**
	 * @return Whether this link utilizes attributes as map keys
	 */
	def usesMapping: Boolean
	/**
	 * @return The english name of this link type. Contains two %s placeholders for class names where the first
	 *         passed class is origin and second one is target.
	 */
	def nameWithClassSlots: String
	/**
	 * @return The role which is considered the owner or parent class when using this link with ownership. Arbitrary if
	 *         this link type doesn't support ownership
	 */
	def fixedOwner: LinkEndRole
	
	
	// COMPUTED	--------------------
	
	/**
	 * @return Whether this link type allows one to place multiple links for a single target
	 */
	def allowsMultipleOrigins = category.allowsMultipleOrigins
	/**
	 * @return Whether this link type allows one to place multiple links for a single origin
	 */
	def allowsMultipleTargets = category.allowsMultipleTargets
	/**
	 * @return The role which is considered the child or sub-class when using this link with ownership. Arbitrary if
	 *         this link type doesn't support ownership
	 */
	def fixedChild = fixedOwner.opposite
}

object LinkType
{
	/**
	 * A common trait for links that have one origin and multiple target items
	 */
	sealed trait ManyToOneType extends LinkType
	{
		override def category = ManyToOne
		override def isOwnable = true
		override def isFixedLinkOrigin = true
		override def fixedOwner = Target
	}
	
	/**
	 * A common trait for links that have a single origin and target item
	 */
	sealed trait OneToOneType extends LinkType
	{
		override def category = OneToOne
		override def isOwnable = true
		override def usesMapping = false
	}
	
	/**
	 * This link type allows one to link multiple targets to one origin without any added features
	 */
	case object BasicManyToOne extends ManyToOneType
	{
		override def id = 1
		override def usesDeprecation = false
		override def usesMapping = false
		override def nameWithClassSlots = "Many %s to one %s"
	}
	/**
	 * This link type allows one to link multiple targets to one origin. The targets support deprecation.
	 */
	case object DeprecatingManyToOne extends ManyToOneType
	{
		override def id = 2
		override def usesDeprecation = true
		override def usesMapping = false
		override def nameWithClassSlots = "Many versioned %s to a single %s"
	}
	/**
	 * This link type allows one to link a single active item per key to one origin. Previous map values will be
	 * deprecated as new ones are added.
	 */
	case object DeprecatingMap extends ManyToOneType
	{
		override def id = 3
		override def usesDeprecation = true
		override def usesMapping = true
		
		override def nameWithClassSlots = "A single %s version per key to one %s"
	}
	/**
	 * This link type allows one to link multiple items to a single link origin. The items must be unique by a
	 * search key, however
	 */
	case object EnforcedMap extends ManyToOneType
	{
		override def id = 4
		override def usesDeprecation = false
		override def usesMapping = true
		
		override def nameWithClassSlots = "A single %s per key to one %s"
	}
	
	/**
	 * This link type allows one to link a single target item to a single origin item.
	 */
	case object EnforcedOneToOne extends OneToOneType
	{
		override def id = 5
		override def isFixedLinkOrigin = false
		override def usesDeprecation = false
		override def fixedOwner = Origin
		override def nameWithClassSlots = "One %s to one %s"
	}
	/**
	 * This link type allows one to link 0-1 target items to a single origin item
	 */
	case object OneToZeroOrOne extends OneToOneType
	{
		override def id = 8
		override def isFixedLinkOrigin = true
		override def usesDeprecation = false
		override def nameWithClassSlots = "One %s to 0-1 %s"
		override def fixedOwner = Origin
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
		override def nameWithClassSlots = "One %s version to one %s"
		override def fixedOwner = Target
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
		override def usesMapping = false
		override def fixedOwner = Origin
		override def nameWithClassSlots = "Many %s to many %s"
	}
	
	
	// ATTRIBUTES	-------------------------
	
	/**
	 * All currently known link types
	 */
	val values: Vector[LinkType] = Vector(BasicManyToOne, DeprecatingManyToOne, DeprecatingMap, EnforcedMap,
		EnforcedOneToOne, DeprecatingOneToOne, BasicManyToMany)
	
	/**
	 * @param id Link type id
	 * @return A link type matching specified id
	 */
	def forId(id: Int) = values.find { _.id == id }
}