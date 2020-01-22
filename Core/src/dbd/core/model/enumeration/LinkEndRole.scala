package dbd.core.model.enumeration

/**
 * A common trait / enum for link ends
 * @author Mikko Hilpinen
 * @since 22.1.2020, v0.1
 */
sealed trait LinkEndRole
{
	/**
	 * @return The role opposite to this one
	 */
	def opposite: LinkEndRole
}

object LinkEndRole
{
	/**
	 * Origin is the class where the links is actually written
	 */
	case object Origin extends LinkEndRole
	{
		override def opposite = Target
	}
	/**
	 * Target is the class where the link points to
	 */
	case object Target extends LinkEndRole
	{
		override def opposite = Origin
	}
}
