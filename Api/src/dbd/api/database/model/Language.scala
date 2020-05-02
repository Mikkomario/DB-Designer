package dbd.api.database.model

import dbd.api.database.Tables
import dbd.core.model.existing
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.StorableFactoryWithValidation

object Language extends StorableFactoryWithValidation[existing.Language]
{
	// IMPLEMENTED	-------------------------------
	
	override def table = Tables.language
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.Language(model("id").getInt,
		model("isoCode").getString)
	
	
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
		existing.Language(newId, code)
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
case class Language(id: Option[Int] = None, isoCode: Option[String] = None) extends StorableWithFactory[existing.Language]
{
	override def factory = Language
	
	override def valueProperties = Vector("id" -> id, "isoCode" -> isoCode)
}