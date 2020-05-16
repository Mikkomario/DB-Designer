package dbd.core.model.combined.language

import dbd.core.model.existing.description.DescriptionLink
import dbd.core.model.existing.language.Language
import dbd.core.model.template.Extender
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Adds descriptive data to a language
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class DescribedLanguage(language: Language, descriptions: Set[DescriptionLink])
	extends Extender[Language] with ModelConvertible
{
	override def wrapped = language
	
	override def toModel = language.toModel +
		Constant("descriptions", descriptions.map { _.toModel }.toVector)
}
