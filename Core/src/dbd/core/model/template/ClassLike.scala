package dbd.core.model.template

/**
 * A common trait for class models
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
trait ClassLike[Info <: ClassInfoLike, Attribute <: AttributeLike[_], Repr <: ClassLike[Info, Attribute, _]]
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
	
	/**
	 * Creates a copy of this class
	 * @param info New info for this class
	 * @param attributes New attributes for this class
	 * @return A copy of this class with specified data
	 */
	protected def makeCopy(info: Info, attributes: Vector[Attribute]): Repr
	
	
	// COMPUTED	----------------------
	
	/**
	 * @return The current name of this class
	 */
	def name = info.name
	
	/**
	 * @return Whether this class should be considered mutable
	 */
	def isMutable = info.isMutable
	
	
	// OTHER	---------------------
	
	/**
	 * @param attribute A new attribute to add
	 * @return A copy of this class with specified attribute added
	 */
	def +(attribute: Attribute) = makeCopy(info, attributes :+ attribute)
}
