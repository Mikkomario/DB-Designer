package dbd.core.database.model

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import dbd.core.database.Tables
import dbd.core.model.existing
import dbd.core.model.existing.database
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, LinkedFactory, FromRowFactoryWithTimestamps}

object Attribute extends LinkedFactory[database.Attribute, database.AttributeConfiguration] with Deprecatable
	with FromRowFactoryWithTimestamps[database.Attribute]
{
	// IMPLEMENTED	-----------------------
	
	override def creationTimePropertyName = "created"
	
	override def nonDeprecatedCondition = notDeletedCondition && AttributeConfiguration.nonDeprecatedCondition
	
	override def childFactory = AttributeConfiguration
	
	override def apply(model: Model[Constant], child: database.AttributeConfiguration) =
		table.requirementDeclaration.validate(model).toTry.map { valid => database.Attribute(valid("id").getInt,
			valid("classID").getInt, child, valid("deletedAfter").instant) }
	
	override def table = Tables.attribute
	
	
	// COMPUTED	--------------------------
	
	/**
	  * @return Condition that only returns non-deleted attributes
	  */
	def notDeletedCondition = deletedAfterColumn.isNull
	
	/**
	  * @return Column that specifies whether and when this attribute is/was deleted
	  */
	def deletedAfterColumn = table("deletedAfter")
	
	
	// OTHER	--------------------------
	
	/**
	 * @param id Attribute id
	 * @return A model with only id set
	 */
	def withId(id: Int) = apply(Some(id))
	
	/**
	 * @param classId Id of target class
	 * @return A model with only class id set
	 */
	def withClassId(classId: Int) = apply(classId = Some(classId))
	
	
	/**
	 * @param classId Id of target class
	 * @return A model ready to be inserted to DB
	 */
	def forInsert(classId: Int) = apply(classId = Some(classId))
}

/**
 * Used for interacting with attribute data in DB
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
case class Attribute(id: Option[Int] = None, classId: Option[Int] = None, deletedAfter: Option[Instant] = None)
	extends StorableWithFactory[database.Attribute]
{
	// IMPLEMENTED	-----------------------
	
	override def factory = Attribute
	
	override def valueProperties = Vector("id" -> id, "classId" -> classId, "deletedAfter" -> deletedAfter)
	
	
	// COMPUTED	--------------------------
	
	/**
	 * @return A copy of this model that has just been marked as deleted
	 */
	def nowDeleted = copy(deletedAfter = Some(Instant.now()))
}
