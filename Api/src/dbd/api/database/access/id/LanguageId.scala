package dbd.api.database.access.id

import dbd.api.database.model.language.LanguageModel
import utopia.flow.datastructure.immutable.Value
import utopia.vault.nosql.access.{SingleIdAccess, UniqueAccess}

/**
  * Used for accessing individual language ids
  * @author Mikko Hilpinen
  * @since 17.5.2020, v2
  */
object LanguageId extends SingleIdAccess[Int]
{
	// IMPLEMENTED	---------------------------
	
	override def target = LanguageModel.target
	
	override def valueToId(value: Value) = value.int
	
	override def table = LanguageModel.table
	
	override def globalCondition = None
	
	
	// OTHER	-------------------------------
	
	/**
	  * @param languageCode Targeted language's ISO-code
	  * @return An access point to that language's id
	  */
	def forIsoCode(languageCode: String) = IdForCode(languageCode)
	
	
	// NESTED	-------------------------------
	
	case class IdForCode(languageCode: String) extends SingleIdAccess[Int] with UniqueAccess[Int]
	{
		// IMPLEMENTED	-----------------------
		
		override def condition = LanguageModel.withIsoCode(languageCode).toCondition
		
		override def target = LanguageId.target
		
		override def valueToId(value: Value) = value.int
		
		override def table = LanguageId.table
	}
}
