package dbd.core.model.enumeration

/**
 * Provides a set of main categories for link types
 * @author Mikko Hilpinen
 * @since 18.1.2020, v0.1
 */
sealed trait LinkTypeCategory
{
	/**
	 * @return Whether this category allows one to place multiple links per target
	 */
	def allowsMultipleOrigins: Boolean
	/**
	 * @return Whether this category allows one to place multiple links per origin
	 */
	def allowsMultipleTargets: Boolean
}

object LinkTypeCategory
{
	/**
	 * This category of links points from one class to 0-n other classes
	 */
	case object OneToMany extends LinkTypeCategory
	{
		override def allowsMultipleOrigins = false
		override def allowsMultipleTargets = true
	}
	
	/**
	 * This category of links always points from one class to 0-1 other classes
	 */
	case object OneToOne extends LinkTypeCategory
	{
		override def allowsMultipleOrigins = false
		override def allowsMultipleTargets = false
	}
	
	/**
	 * This category of links points from 0-n classes to 0-n classes
	 */
	case object ManyToMany extends LinkTypeCategory
	{
		override def allowsMultipleOrigins = true
		override def allowsMultipleTargets = true
	}
}
