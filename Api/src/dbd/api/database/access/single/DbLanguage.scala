package dbd.api.database.access.single

import dbd.api.database.model.language
import dbd.core.model.existing
import utopia.flow.generic.ValueConversions._
import utopia.vault.nosql.access.SingleModelAccessById

/**
  * Used for accessing individual languages
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
object DbLanguage extends SingleModelAccessById[existing.language.Language, Int]
{
	// IMPLEMENTED	--------------------------------
	
	override def idToValue(id: Int) = id
	
	override def factory = language.LanguageModel
}
