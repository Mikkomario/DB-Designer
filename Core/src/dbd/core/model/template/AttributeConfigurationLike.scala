package dbd.core.model.template

import dbd.core.model.enumeration.AttributeType

/**
 * A common trait for attribute configuration models
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
trait AttributeConfigurationLike
{
	// ABSTRACT	---------------------
	
	/**
	 * @return Current name of described attribute
	 */
	def name: String
	/**
	 * @return Current data type of described attribute
	 */
	def dataType: AttributeType
	/**
	 * @return Whether described attribute should be considered optional
	 */
	def isOptional: Boolean
	/**
	 * @return Whether described attribute is used as a search key
	 */
	def isSearchKey: Boolean
	
	
	// OPERATORS	-----------------
	
	/**
	 * @param other Another configuration
	 * @return Whether these two configurations are alike
	 */
	def ~==(other: AttributeConfigurationLike) = name == other.name && dataType == other.dataType &&
		isOptional == other.isOptional && isSearchKey == other.isSearchKey
}
