package dbd.core.model.template

/**
 * A common trait for class models
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
trait ClassLike[+Info <: ClassInfoLike, +Attribute <: AttributeLike[_]]
{
	// ABSTRACT	----------------------
	
	/**
	 * @return Basic information about this class
	 */
	def info: Info
	
	/**
	 * @return Attributes associated with this class
	 */
	def attributes: Vector[Attribute]
	
	
	// COMPUTED	----------------------
	
	/**
	 * @return The current name of this class
	 */
	def name = info.name
	
	/**
	 * @return Whether this class should be considered mutable
	 */
	def isMutable = info.isMutable
}
