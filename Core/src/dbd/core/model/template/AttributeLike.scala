package dbd.core.model.template

/**
 * A common trait for attribute models
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
trait AttributeLike[+Configuration <: AttributeConfigurationLike]
{
	// ABSTRACT	-------------------
	
	def configuration: Configuration
	
	
	// COMPUTED	-------------------
	
	/**
	 * @return Current name of this attribute
	 */
	def name = configuration.name
	
	/**
	 * @return Current data type of this attribute
	 */
	def dataType = configuration.dataType
	
	/**
	 * @return Whether this attribute should be considered optional at this time
	 */
	def isOptional = configuration.isOptional
	
	/**
	 * @return Whether this attribute is intended to be used as a search key
	 */
	def isSearchKey = configuration.isSearchKey
}
