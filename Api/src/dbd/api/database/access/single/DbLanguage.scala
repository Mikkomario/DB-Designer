package dbd.api.database.access.single

import dbd.api.database.access.id.LanguageId
import dbd.api.database.model.language
import dbd.core.model.error.NoDataFoundException
import dbd.core.model.existing
import dbd.core.model.post.NewLanguageProficiency
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.CollectionExtensions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.SingleModelAccessById

import scala.util.{Failure, Success}

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
	
	
	// OTHER	------------------------------------
	
	/**
	  * Validates the proposed language proficiencies, making sure all language ids and codes are valid
	  * @param proficiencies Proposed proficiencies
	  * @param connection DB Connection (implicit)
	  * @return List of language id -> familiarity pairs. Failure if some of the ids or codes were invalid
	  */
	def validateProposedProficiencies(proficiencies: Vector[NewLanguageProficiency])(implicit connection: Connection) =
	{
		proficiencies.tryMap { proficiency =>
			val languageId = proficiency.language match
			{
				case Right(languageId) =>
					if (apply(languageId).isDefined)
						Success(languageId)
					else
						Failure(new NoDataFoundException(s"$languageId is not a valid language id"))
				case Left(languageCode) =>
					LanguageId.forIsoCode(languageCode).pull.toTry {
						new NoDataFoundException(s"$languageCode is not a valid language code") }
			}
			languageId.map { _ -> proficiency.familiarity }
		}
	}
}
