package dbd.api.rest.resource.description

import dbd.api.database.access.many.{DbDescriptions, DbLanguages}
import dbd.core.model.combined.language.DescribedLanguage
import dbd.core.model.existing.description.DescriptionLink
import dbd.core.model.existing.language.Language
import utopia.vault.database.Connection

/**
  * Used for accessing all specified languages
  * @author Mikko Hilpinen
  * @since 20.5.2020, v2
  */
object LanguagesNode extends PublicDescriptionsNode[Language, DescribedLanguage]
{
	// IMPLEMENTED	--------------------------------
	
	override val name = "languages"
	
	override protected def items(implicit connection: Connection) = DbLanguages.all
	
	override protected def descriptionsAccess = DbDescriptions.ofAllLanguages
	
	override protected def idOf(item: Language) = item.id
	
	override protected def combine(item: Language, descriptions: Set[DescriptionLink]) =
		DescribedLanguage(item, descriptions)
}
