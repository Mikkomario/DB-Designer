package dbd.core.model.existing.database

import dbd.core.model.enumeration.AttributeType
import dbd.core.model.template.AttributeConfigurationLike

import scala.collection.immutable.VectorBuilder

/**
 * Specifies a configuration (name, type etc.) that should be used for an attribute
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param id Unique id of this configuration
 * @param attributeId Id of the attribute described by this configuration
 * @param name The name of the attribute
 * @param dataType Data type for the attribute
 * @param isOptional Whether the attribute should be optional
 * @param isSearchKey Whether the attribute is used as a search key
 */
case class AttributeConfiguration(id: Int, attributeId: Int, name: String, dataType: AttributeType,
								  isOptional: Boolean, isSearchKey: Boolean) extends AttributeConfigurationLike
{
	override def toString =
	{
		val extrasBuilder = new VectorBuilder[String]
		if (isOptional)
			extrasBuilder += "optional"
		if (isSearchKey)
			extrasBuilder += "search key"
		val extras = extrasBuilder.result()
		s"$name ($dataType)${if (extras.isEmpty) "" else s"(${extras.mkString(", ")})"}"
	}
}
