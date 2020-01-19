package dbd.core.model.partial

import dbd.core.model.enumeration.AttributeType
import dbd.core.model.existing.AttributeConfiguration
import dbd.core.model.template.AttributeConfigurationLike

/**
 * Represents an attribute configuration that hasn't yet been saved
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
case class NewAttributeConfiguration(name: String, dataType: AttributeType, isOptional: Boolean = false,
									 isSearchKey: Boolean = false) extends AttributeConfigurationLike
{
	/**
	 * @param id Id for this configuration
	 * @param attributeId Id of the described attribute
	 * @return A full configuration model based on this one
	 */
	def withId(id: Int, attributeId: Int) = AttributeConfiguration(id, attributeId, name, dataType, isOptional, isSearchKey)
}