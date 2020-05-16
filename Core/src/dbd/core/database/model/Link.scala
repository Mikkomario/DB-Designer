package dbd.core.database.model

import java.time.Instant

import dbd.core.database.Tables
import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.existing.database
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, LinkedStorableFactory, RowFactoryWithTimestamps}

object Link extends LinkedStorableFactory[database.Link, database.LinkConfiguration] with Deprecatable
	with RowFactoryWithTimestamps[database.Link]
{
	// ATTRIBUTES	---------------------------
	
	/**
	 * @return Name of deletion variable
	 */
	val deletedAfterVarName = "deletedAfter"
	
	
	// IMPLEMENTED	---------------------------
	
	override def creationTimePropertyName = "created"
	
	override def childFactory = LinkConfiguration
	
	override def apply(model: Model[Constant], child: database.LinkConfiguration) =
		table.requirementDeclaration.validate(model).toTry.map { valid =>
			database.Link(valid("id").getInt, valid("databaseId").getInt, child, valid(deletedAfterVarName).instant)
		}
	
	override def nonDeprecatedCondition = childFactory.nonDeprecatedCondition && notDeletedCondition
	
	override def table = Tables.link
	
	
	// COMPUTED	--------------------------------
	
	/**
	  * @return Column that specifies whether a row is deleted or not and when it was deleted
	  */
	def deletedAfterColumn = table(deletedAfterVarName)
	
	/**
	 * @return A condition that only returns non-deleted links
	 */
	def notDeletedCondition = deletedAfterColumn.isNull
	
	/**
	 * @return A model that has just been marked as deleted
	 */
	def nowDeleted = apply(deletedAfter = Some(Instant.now()))
	
	
	// OTHER	--------------------------------
	
	/**
	 * @param databaseId Id of target database
	 * @return A model with only database id set
	 */
	def withDatabaseId(databaseId: Int) = apply(databaseId = Some(databaseId))
	
	/**
	 * @return A model ready to be inserted to DB
	 */
	def forInsert(databaseId: Int) = apply(None, Some(databaseId))
}

/**
 * Used for interacting with links in DB
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
case class Link(id: Option[Int] = None, databaseId: Option[Int] = None, deletedAfter: Option[Instant] = None)
	extends StorableWithFactory[database.Link]
{
	override def factory = Link
	
	override def valueProperties = Vector("id" -> id, "databaseId" -> databaseId, Link.deletedAfterVarName -> deletedAfter)
}
