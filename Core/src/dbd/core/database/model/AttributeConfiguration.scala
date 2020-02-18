package dbd.core.database.model

import java.time.Instant

import dbd.core.database.Tables
import dbd.core.model.enumeration.AttributeType
import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.partial.NewAttributeConfiguration
import utopia.flow.datastructure.template.{Model, Property}
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, RowFactoryWithTimestamps, StorableFactory}

object AttributeConfiguration extends StorableFactory[existing.AttributeConfiguration] with Deprecatable
	with RowFactoryWithTimestamps[existing.AttributeConfiguration]
{
	// ATTRIBUTES	---------------------------
	
	override val nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	
	// IMPLEMENTED	---------------------------
	
	override def creationTimePropertyName = "created"
	
	override def apply(model: Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		// Data type must be parseable
		val typeId = valid("dataType").getInt
		AttributeType.withId(typeId).map { attType => existing.AttributeConfiguration(valid("id").getInt,
			valid("attributeId").getInt, valid("name").getString, attType, valid("isOptional").getBoolean,
			valid("isSearchKey").getBoolean)
		}
	}
	
	override def table = Tables.attributeConfiguration
	
	
	// OTHER	-----------------------------
	
	/**
	 * @param attId Id of target attribute
	 * @return A model with only attribute id set
	 */
	def withAttributeId(attId: Int) = apply(attributeId = Some(attId))
	
	/**
	 * @return A model that has been marked just deprecated
	 */
	def deprecatedNow = apply(deprecatedAfter = Some(Instant.now()))
	
	/**
	 * Creates a model for inserting a new row to DB
	 * @param attributeId Id of targeted attribute
	 * @param model A new configuration for the attribute
	 * @return A model ready to be inserted
	 */
	def forInsert(attributeId: Int, model: NewAttributeConfiguration) = apply(None,
		Some(attributeId), Some(model.name), Some(model.dataType), Some(model.isOptional), Some(model.isSearchKey))
}

/**
 * Used for interacting with attribute configurations in DB
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 */
case class AttributeConfiguration(id: Option[Int] = None, attributeId: Option[Int] = None, name: Option[String] = None,
								  dataType: Option[AttributeType] = None, isOptional: Option[Boolean] = None,
								  isSearchKey: Option[Boolean] = None, deprecatedAfter: Option[Instant] = None)
	extends StorableWithFactory[existing.AttributeConfiguration]
{
	// IMPLEMENTED	-----------------------
	
	override def factory = AttributeConfiguration
	
	override def valueProperties = Vector("id" -> id, "attributeId" -> attributeId, "name" -> name,
		"dataType" -> dataType.map { _.id }, "isOptional" -> isOptional, "isSearchKey" -> isSearchKey,
		"deprecatedAfter" -> deprecatedAfter)
}
