package dbd.api.database.factory.user

import dbd.api.database.model.language.LanguageModel
import dbd.core.model.combined.user.FullUserLanguage
import dbd.core.model.existing.language.Language
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.nosql.factory.LinkedFactory

/**
  * Used for reading user languages with language data included
  * @author Mikko Hilpinen
  * @since 17.5.2020, v2
  */
object FullUserLanguageFactory extends LinkedFactory[FullUserLanguage, Language]
{
	// IMPLEMENTED	--------------------------
	
	override def childFactory = LanguageModel
	
	override def apply(model: Model[Constant], child: Language) =
		UserLanguageFactory(model).map { FullUserLanguage(_, child) }
	
	override def table = UserLanguageFactory.table
}
