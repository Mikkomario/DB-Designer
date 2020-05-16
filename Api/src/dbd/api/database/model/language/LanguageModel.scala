package dbd.api.database.model.language

import dbd.api.database.Tables
import dbd.core.model.existing.language
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.StorableFactoryWithValidation

object LanguageModel extends StorableFactoryWithValidation[language.Language]
{
	// ATTRIBUTES	-------------------------------
	
	/**
	  * Name of the attribute that contains the language ISO-code
	  */
	val isoCodeAttName = "isoCode"
	
	
	// IMPLEMENTED	-------------------------------
	
	override def table = Tables.language
	
	override protected def fromValidatedModel(model: Model[Constant]) = language.Language(model("id").getInt,
		model("isoCode").getString)
	
	
	// COMPUTED	-----------------------------------
	
	/**
	  * @return Column that contains the language ISO-code
	  */
	def isoCodeColumn = table(isoCodeAttName)
	
	
	// OTHER	-----------------------------------
	
	/**
	  * @param code Language ISO-code
	  * @return A model with only ISO-code set
	  */
	def withIsoCode(code: String) = apply(isoCode = Some(code))
	
	/**
	  * Inserts a new language to the DB (please make sure no such language exists before inserting one)
	  * @param code Language ISO-code
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted language
	  */
	def insert(code: String)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(code)).insert().getInt
		language.Language(newId, code)
	}
	
	/**
	  * Reads language from DB or inserts one if it doesn't exist
	  * @param code Language ISO-code
	  * @param connection DB Connection (implicit)
	  * @return Read or created language
	  */
	def getOrInsert(code: String)(implicit connection: Connection) =
	{
		get(withIsoCode(code).toCondition).getOrElse(insert(code))
	}
}

/**
  * Used for interacting with language data in DB
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class LanguageModel(id: Option[Int] = None, isoCode: Option[String] = None) extends StorableWithFactory[language.Language]
{
	import LanguageModel._
	
	override def factory = LanguageModel
	
	override def valueProperties = Vector("id" -> id, isoCodeAttName -> isoCode)
}