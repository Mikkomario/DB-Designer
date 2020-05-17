package dbd.api.database.factory.user

import dbd.api.database.Tables
import dbd.core.model.enumeration.LanguageFamiliarity
import dbd.core.model.existing.user.UserLanguage
import dbd.core.model.partial.user.UserLanguageData
import utopia.flow.datastructure.template.{Model, Property}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.FromRowModelFactory

/**
  * Used for reading user language links from the DB
  * @author Mikko Hilpinen
  * @since 17.5.2020, v2
  */
object UserLanguageFactory extends FromRowModelFactory[UserLanguage]
{
	// Familiarity must be parseable
	override def apply(model: Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		LanguageFamiliarity.forId(valid("familiarityId")).map { familiarity =>
			UserLanguage(valid("id"), UserLanguageData(valid("userId"), valid("languageId"), familiarity))
		}
	}
	
	override def table = Tables.userLanguage
}
