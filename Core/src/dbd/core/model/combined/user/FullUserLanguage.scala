package dbd.core.model.combined.user

import dbd.core.model.existing.language.Language
import dbd.core.model.existing.user.UserLanguage

/**
  * A user language will full language data included
  * @author Mikko Hilpinen
  * @since 17.5.2020, v2
  */
case class FullUserLanguage(wrapped: UserLanguage, language: Language) extends FullUserLanguageLike[Language]