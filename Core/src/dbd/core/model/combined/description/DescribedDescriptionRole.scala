package dbd.core.model.combined.description

import dbd.core.model.enumeration.DescriptionRole
import dbd.core.model.existing.description.DescriptionLink
import utopia.flow.datastructure.immutable.{Model, Value}
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Combines a description role with some descriptions
  * @author Mikko Hilpinen
  * @since 20.5.2020, v2
  */
case class DescribedDescriptionRole(role: DescriptionRole, descriptions: Set[DescriptionLink]) extends ModelConvertible
{
	// Uses role json keys to describe the roles (so that the user won't need to use recursion
	// or something to interpret the ids)
	override def toModel =
	{
		val descriptionProperties: Vector[(String, Value)] = descriptions.toVector.map { link =>
			val valueModel = Model(Vector("text" -> link.description.text, "language_id" -> link.description.languageId,
				"role_id" -> link.description.role.id))
			link.description.role.jsonKey -> valueModel
		}
		Model(("id" -> (role.id: Value)) +: descriptionProperties)
	}
}
