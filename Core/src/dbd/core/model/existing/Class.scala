package dbd.core.model.existing

import utopia.flow.util.CollectionExtensions._
import dbd.core.model.template.ClassLike

/**
 * Specifies the basic structure of a model
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param id Unique id of this class
 * @param info Basic info about this class
 * @param attributes Attribute specifications for this class
 */
case class Class(id: Int, info: ClassInfo, attributes: Vector[Attribute]) extends ClassLike[ClassInfo, Attribute, Class]
{
	override protected def makeCopy(info: ClassInfo, attributes: Vector[Attribute]) = Class(id, info, attributes)
	
	/**
	 * Updates a singular attribute configuration in this class
	 * @param newConfiguration A new configuration for an attribute
	 * @return A modified version of this class
	 */
	def update(newConfiguration: AttributeConfiguration) = copy(attributes = attributes.mapFirstWhere {
		_.id == newConfiguration.attributeId } { _.withConfiguration(newConfiguration) })
}
