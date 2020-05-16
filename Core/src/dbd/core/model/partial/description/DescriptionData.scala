package dbd.core.model.partial.description

import dbd.core.model.enumeration.DescriptionRole
import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Contains basic data about a description
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param role Role of this description
  * @param languageId Id of the language used in this description
  * @param text Description text
  * @param authorId Id of the user who wrote this description (optional)
  */
case class DescriptionData(role: DescriptionRole, languageId: Int, text: String, authorId: Option[Int] = None)
	extends ModelConvertible
{
	override def toModel = Model("role" -> role.id, "text" -> text, "language_id" -> languageId,
		"author_id" -> authorId)
}