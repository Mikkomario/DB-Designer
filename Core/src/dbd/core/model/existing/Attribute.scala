package dbd.core.model.existing

import dbd.core.model.template.AttributeLike

/**
 * Represents a single attribute / parameter for a class
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param configuration Current configuration for this attribute
 */
case class Attribute(id: Int, classId: Int, configuration: AttributeConfiguration)
	extends AttributeLike[AttributeConfiguration]
