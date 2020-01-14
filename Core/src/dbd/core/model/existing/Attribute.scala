package dbd.core.model.existing

import java.time.Instant

import dbd.core.model.template.AttributeLike

/**
 * Represents a single attribute / parameter for a class
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param configuration Current configuration for this attribute
 */
case class Attribute(id: Int, classId: Int, configuration: AttributeConfiguration, deletedAfter: Option[Instant])
	extends AttributeLike[AttributeConfiguration]
{
	/**
	 * @param newConfiguration A new configuration for this attribute
	 * @return A copy of this attribute with specified configuration
	 */
	def withConfiguration(newConfiguration: AttributeConfiguration) = copy(configuration = newConfiguration)
}
