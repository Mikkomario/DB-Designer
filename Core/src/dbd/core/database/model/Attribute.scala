package dbd.core.database.model

import utopia.flow.generic.ValueConversions._
import dbd.core.database.Tables
import dbd.core.model.existing
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.model.immutable.factory.{Deprecatable, LinkedStorableFactory}

object Attribute extends LinkedStorableFactory[existing.Attribute, existing.AttributeConfiguration] with Deprecatable
{
	// IMPLEMENTED	-----------------------
	
	override def nonDeprecatedCondition = AttributeConfiguration.nonDeprecatedCondition
	
	override def childFactory = AttributeConfiguration
	
	override def apply(model: Model[Constant], child: existing.AttributeConfiguration) =
		table.requirementDeclaration.validate(model).toTry.map { valid => existing.Attribute(valid("id").getInt,
			valid("classID").getInt, child) }
	
	override def table = Tables.attribute
	
	
	// OTHER	--------------------------
	
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
case class Attribute(id: Option[Int] = None, classId: Option[Int] = None) extends StorableWithFactory[existing.Attribute]
{
	override def factory = Attribute
	
	override def valueProperties = Vector("id" -> id, "classId" -> classId)
}
