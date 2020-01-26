package dbd.core.model.template

/**
 * A common trait for class info models
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
trait ClassInfoLike
{
	// ABSTRACT	------------------
	
	/**
	 * @return Name for described class
	 */
	def name: String
	
	/**
	 * @return Whether described class should be considered mutable
	 */
	def isMutable: Boolean
	
	
	// OTHER	-------------------
	
	/**
	 * @param other Another class info
	 * @return Whether these two info are alike
	 */
	def ~==(other: ClassInfoLike) = name == other.name && isMutable == other.isMutable
}
