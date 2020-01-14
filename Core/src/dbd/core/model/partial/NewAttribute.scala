package dbd.core.model.partial

import dbd.core.model.existing.Attribute
import dbd.core.model.template.AttributeLike

/**
 * Represents a new attribute that hasn't been saved yet
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
case class NewAttribute(configuration: NewAttributeConfiguration) extends AttributeLike[NewAttributeConfiguration]
{
	/**
	 * @param id Id of this attribute
	 * @param classId Id of the class targeted by this attribute
	 * @param currentConfigurationId Id of the current configuration for this attribute
	 * @return An attribute with id data included
	 */
	def withId(id: Int, classId: Int, currentConfigurationId: Int) = Attribute(id, classId,
		configuration.withId(currentConfigurationId, id), None)
}