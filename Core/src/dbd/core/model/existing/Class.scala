package dbd.core.model.existing

import java.time.Instant

import utopia.flow.util.CollectionExtensions._
import dbd.core.model.template.ClassLike

/**
 * Specifies the basic structure of a model
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param id Unique id of this class
 * @param info Basic info about this class
 * @param attributes Attribute specifications for this class
 * @param deletedAfter Whether this class should be considered deleted and when this deletion happened (default = None)
 */
case class Class(id: Int, info: ClassInfo, attributes: Vector[Attribute], deletedAfter: Option[Instant] = None)
	extends ClassLike[ClassInfo, Attribute, Class]
{
	override def toString = s"Class $info: [${attributes.mkString(", ")}]"
	
	override def makeCopy(info: ClassInfo, attributes: Vector[Attribute]) = Class(id, info, attributes)
	
	/**
	 * Updates a singular attribute configuration in this class
	 * @param newConfiguration A new configuration for an attribute
	 * @return A modified version of this class
	 */
	def update(newConfiguration: AttributeConfiguration) = copy(attributes = attributes.mapFirstWhere {
		_.id == newConfiguration.attributeId } { _.withConfiguration(newConfiguration) })
}
