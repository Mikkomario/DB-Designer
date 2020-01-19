package dbd.core.database.model

import java.time.Instant

import utopia.flow.util.CollectionExtensions._
import dbd.core.database.Tables
import dbd.core.model.enumeration.LinkType
import dbd.core.model.error.NoSuchTypeException
import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.partial.NewLinkConfiguration
import utopia.flow.datastructure.template
import utopia.flow.datastructure.template.Property
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.model.immutable.factory.{Deprecatable, StorableFactory}

object LinkConfiguration extends StorableFactory[existing.LinkConfiguration] with Deprecatable
{
	// IMPLEMENTED	---------------------------
	
	override def nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override def apply(model: template.Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		// Link type must be parseable
		LinkType.forId(valid("linkType").getInt).toTry(
			new NoSuchTypeException(s"No link type for id ${valid("linkType")}")).map { linkType =>
			existing.LinkConfiguration(valid("id").getInt, valid("linkId").getInt, valid("name").getString, linkType,
				valid("originClassId").getInt, valid("targetClassId").getInt, valid("isOwned").getBoolean,
				valid("deprecatedAfter").instant) }
	}
	
	override def table = Tables.linkConfiguration
	
	
	// COMPUTED	-------------------------------
	
	/**
	 * @return a model that has just been marked as deprecated
	 */
	def nowDeprecated = apply(deprecatedAfter = Some(Instant.now()))
	
	
	// OTHER	-------------------------------
	
	/**
	 * @param linkId Targeted link's id
	 * @return A model with only link id set
	 */
	def withLinkId(linkId: Int) = apply(linkId = Some(linkId))
	
	/**
	 * Creates a model to be inserted to DB
	 * @param linkId Id of modified link
	 * @param newData New configuration data for the link
	 * @return A model ready to be inserted to DB
	 */
	def forInsert(linkId: Int, newData: NewLinkConfiguration) = apply(None, Some(linkId),
		Some(newData.name), Some(newData.linkType), Some(newData.originClassId), Some(newData.targetClassId),
		Some(newData.isOwned))
}

/**
 * Used for interacting with link configuration DB data
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
case class LinkConfiguration(id: Option[Int] = None, linkId: Option[Int] = None, name: Option[String] = None,
							 linkType: Option[LinkType] = None, originClassId: Option[Int] = None,
							 targetClassId: Option[Int] = None, isOwned: Option[Boolean] = None,
							 deprecatedAfter: Option[Instant] = None)
	extends StorableWithFactory[existing.LinkConfiguration]
{
	override def factory = LinkConfiguration
	
	override def valueProperties = Vector("id" -> id, "linkId" -> linkId, "name" -> name,
		"linkType" -> linkType.map { _.id }, "originClassId" -> originClassId, "targetClassId" -> targetClassId,
		"isOwned" -> isOwned, "deprecatedAfter" -> deprecatedAfter)
}
