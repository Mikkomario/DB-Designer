package dbd.core.database.model

import java.time.Instant

import dbd.core
import dbd.core.database.Tables
import utopia.flow.util.CollectionExtensions._
import utopia.flow.generic.ValueConversions._
import dbd.core.model.AttributeType
import dbd.core.model.error.NoSuchTypeException
import utopia.flow.datastructure.template.{Model, Property}
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.model.immutable.factory.{Deprecatable, StorableFactory}

object AttributeConfiguration extends StorableFactory[core.model.AttributeConfiguration] with Deprecatable
{
	// ATTRIBUTES	---------------------------
	
	override val nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	
	// IMPLEMENTED	---------------------------
	
	override def apply(model: Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		// Data type must be parseable
		val typeId = valid("dataType").getInt
		AttributeType.forId(typeId).toTry(new NoSuchTypeException(s"No attribute type for id $typeId")).map { attType =>
			core.model.AttributeConfiguration(valid("name").getString, attType, valid("isOptional").getBoolean,
				valid("isSearchKey").getBoolean)
		}
	}
	
	override def table = Tables.attributeConfiguration
}

/**
 * Used for interacting with attribute configurations in DB
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 */
case class AttributeConfiguration(id: Option[Int] = None, attributeId: Option[Int] = None, name: Option[String] = None,
								  dataType: Option[AttributeType] = None, isOptional: Option[Boolean] = None,
								  isSearchKey: Option[Boolean] = None, deprecatedAfter: Option[Instant] = None)
	extends StorableWithFactory[core.model.AttributeConfiguration]
{
	override def factory = AttributeConfiguration
	
	override def valueProperties = Vector("id" -> id, "attributeId" -> attributeId, "name" -> name,
		"dataType" -> dataType.map { _.id }, "isOptional" -> isOptional, "isSearchKey" -> isSearchKey,
		"deprecatedAfter" -> deprecatedAfter)
}
