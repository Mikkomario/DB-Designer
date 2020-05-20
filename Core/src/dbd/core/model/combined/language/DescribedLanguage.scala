package dbd.core.model.combined.language

import dbd.core.model.combined.description.Described
import dbd.core.model.existing.description.DescriptionLink
import dbd.core.model.existing.language.Language

/**
  * Adds descriptive data to a language
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class DescribedLanguage(language: Language, descriptions: Set[DescriptionLink]) extends Described[Language]
{
	override def wrapped = language
}
