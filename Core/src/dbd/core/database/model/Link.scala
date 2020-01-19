package dbd.core.database.model

import java.time.Instant

import dbd.core.database.Tables
import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.model.immutable.factory.{Deprecatable, LinkedStorableFactory}

object Link extends LinkedStorableFactory[existing.Link, existing.LinkConfiguration] with Deprecatable
{
	override def childFactory = LinkConfiguration
	
	override def apply(model: Model[Constant], child: existing.LinkConfiguration) =
		table.requirementDeclaration.validate(model).toTry.map { valid =>
			existing.Link(valid("id").getInt, child, valid("deletedAfter").instant)
		}
	
	override def nonDeprecatedCondition = childFactory.nonDeprecatedCondition && table("deletedAfter").isNull
	
	override def table = Tables.link
}

/**
 * Used for interacting with links in DB
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
case class Link(id: Option[Int] = None, deletedAfter: Option[Instant] = None) extends StorableWithFactory[existing.Link]
{
	override def factory = Link
	
	override def valueProperties = Vector("id" -> id, "deletedAfter" -> deletedAfter)
}
