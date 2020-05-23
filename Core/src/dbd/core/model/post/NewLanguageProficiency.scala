package dbd.core.model.post

import dbd.core.model.enumeration.LanguageFamiliarity
import dbd.core.model.error.IllegalPostModelException
import utopia.flow.datastructure.immutable.{Model, ModelDeclaration, PropertyDeclaration, Value}
import utopia.flow.datastructure.template
import utopia.flow.datastructure.template.Property
import utopia.flow.generic.{FromModelFactory, IntType, ModelConvertible}
import utopia.flow.generic.ValueConversions._
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.util.StringExtensions._

import scala.util.{Failure, Success}

object NewLanguageProficiency extends FromModelFactory[NewLanguageProficiency]
{
	private val schema = ModelDeclaration(PropertyDeclaration("familiarity_id", IntType))
	
	override def apply(model: template.Model[Property]) = schema.validate(model).toTry.flatMap { valid =>
		// Familiarity must be recognized
		LanguageFamiliarity.forId(valid("familiarity_id")).flatMap { familiarity =>
			// Either language id or language code must be specified
			val languageId = valid("language_id").int
			val languageCode = valid("language_code").string.flatMap { _.trim.notEmpty }
			if (languageId.isEmpty && languageCode.isEmpty)
				Failure(new IllegalPostModelException("Either language_id (int) or language_code (string) must be specified"))
			else
			{
				val language = languageId match
				{
					case Some(id) => Right(id)
					case None => Left(languageCode.get)
				}
				Success(NewLanguageProficiency(language, familiarity))
			}
		}
	}
}

/**
  * Used for posting language proficiency levels for users
  * @author Mikko Hilpinen
  * @since 17.5.2020, v2
  */
case class NewLanguageProficiency(language: Either[String, Int], familiarity: LanguageFamiliarity) extends ModelConvertible
{
	override def toModel =
	{
		val languageValuePair: (String, Value) = language match
		{
			case Right(id) => "language_id" -> id
			case Left(code) => "language_code" -> code
		}
		Model(Vector(languageValuePair, "familiarity_id" -> familiarity.id))
	}
}