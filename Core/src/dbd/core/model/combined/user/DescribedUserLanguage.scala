package dbd.core.model.combined.user

import dbd.core.model.combined.language.DescribedLanguage
import dbd.core.model.existing.user.UserLanguage

/**
  * A user language link with language data and language descriptions included
  * @author Mikko Hilpinen
  * @since 17.5.2020, v2
  */
case class DescribedUserLanguage(wrapped: UserLanguage, language: DescribedLanguage)
	extends FullUserLanguageLike[DescribedLanguage]