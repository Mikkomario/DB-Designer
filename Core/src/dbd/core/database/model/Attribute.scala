package dbd.core.database.model

import utopia.flow.generic.ValueConversions._
import dbd.core
import dbd.core.database.Tables
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.model.immutable.factory.{Deprecatable, LinkedStorableFactory}

object Attribute extends LinkedStorableFactory[core.model.Attribute, core.model.AttributeConfiguration] with Deprecatable
{
	override def nonDeprecatedCondition = AttributeConfiguration.nonDeprecatedCondition
	
	override def childFactory = AttributeConfiguration
	
	override def apply(model: Model[Constant], child: core.model.AttributeConfiguration) =
		table.requirementDeclaration.validate(model).toTry.map { _ => core.model.Attribute(child) }
	
	override def table = Tables.attribute
}

/**
 * Used for interacting with attribute data in DB
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
case class Attribute(id: Option[Int] = None, classId: Option[Int] = None) extends StorableWithFactory[core.model.Attribute]
{
	override def factory = Attribute
	
	override def valueProperties = Vector("id" -> id, "classId" -> classId)
}
