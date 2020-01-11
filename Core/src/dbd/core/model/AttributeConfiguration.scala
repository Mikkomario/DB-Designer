package dbd.core.model

/**
 * Specifies a configuration (name, type etc.) that should be used for an attribute
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param name The name of the attribute
 * @param dataType Data type for the attribute
 * @param isOptional Whether the attribute should be optional
 * @param isSearchKey Whether the attribute is used as a search key
 */
case class AttributeConfiguration(name: String, dataType: AttributeType, isOptional: Boolean = false,
								  isSearchKey: Boolean = false)
