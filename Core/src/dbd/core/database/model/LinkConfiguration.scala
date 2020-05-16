package dbd.core.database.model

import java.time.Instant

import utopia.flow.util.CollectionExtensions._
import dbd.core.database.Tables
import dbd.core.model.enumeration.LinkEndRole.{Origin, Target}
import dbd.core.model.enumeration.LinkType
import dbd.core.model.error.NoSuchTypeException
import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.existing.database
import dbd.core.model.partial.database.NewLinkConfiguration
import utopia.flow.datastructure.template
import utopia.flow.datastructure.template.Property
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, RowFactoryWithTimestamps, StorableFactory}

object LinkConfiguration extends StorableFactory[database.LinkConfiguration] with Deprecatable with RowFactoryWithTimestamps[database.LinkConfiguration]
{
	// IMPLEMENTED	---------------------------
	
	override def nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override def creationTimePropertyName = "created"
	
	override def apply(model: template.Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		// Link type must be parseable
		LinkType.forId(valid("linkType").getInt).toTry(
			new NoSuchTypeException(s"No link type for id '${valid("linkType")}'")).map { linkType =>
			database.LinkConfiguration(valid("id").getInt, valid("linkId").getInt, linkType,
				valid("originClassId").getInt, valid("targetClassId").getInt, valid("nameInOrigin").string,
				valid("nameInTarget").string, valid("isOwned").getBoolean, valid("mappingAttributeId").int,
				valid("deprecatedAfter").instant) }
	}
	
	override def table = Tables.linkConfiguration
	
	
	// COMPUTED	-------------------------------
	
	/**
	 * @return a model that has just been marked as deprecated
	 */
	def nowDeprecated = apply(deprecatedAfter = Some(Instant.now()))
	
	/**
	 * @return The columns one can connect a class to, matched with a connection role
	 */
	def classConnectColumns = Map(Origin -> table("originClassId"), Target -> table("targetClassId"))
	
	
	// OTHER	-------------------------------
	
	/**
	 * @param linkId Targeted link's id
	 * @return A model with only link id set
	 */
	def withLinkId(linkId: Int) = apply(linkId = Some(linkId))
	
	/**
	 * @param attId Attribute id
	 * @return A model with specified attribute id set as mapping key id
	 */
	def withMappingKeyAttributeId(attId: Int) = apply(mappingAttributeId = Some(attId))
	
	/**
	 * Creates a model to be inserted to DB
	 * @param linkId Id of modified link
	 * @param newData New configuration data for the link
	 * @return A model ready to be inserted to DB
	 */
	def forInsert(linkId: Int, newData: NewLinkConfiguration) = apply(None, Some(linkId),
		Some(newData.linkType), Some(newData.originClassId), Some(newData.targetClassId), newData.nameInOrigin,
		newData.nameInTarget, Some(newData.isOwned), newData.mappingKeyAttributeId)
}

/**
 * Used for interacting with link configuration DB data
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
case class LinkConfiguration(id: Option[Int] = None, linkId: Option[Int] = None, linkType: Option[LinkType] = None,
							 originClassId: Option[Int] = None, targetClassId: Option[Int] = None,
							 nameInOrigin: Option[String] = None, nameInTarget: Option[String] = None,
							 isOwned: Option[Boolean] = None, mappingAttributeId: Option[Int] = None,
							 deprecatedAfter: Option[Instant] = None)
	extends StorableWithFactory[database.LinkConfiguration]
{
	override def factory = LinkConfiguration
	
	override def valueProperties = Vector("id" -> id, "linkId" -> linkId,
		"linkType" -> linkType.map { _.id }, "originClassId" -> originClassId, "targetClassId" -> targetClassId,
		"nameInOrigin" -> nameInOrigin, "nameInTarget" -> nameInTarget, "isOwned" -> isOwned,
		"mappingAttributeId" -> mappingAttributeId, "deprecatedAfter" -> deprecatedAfter)
}
