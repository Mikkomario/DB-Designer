package dbd.api.database.access.many

import dbd.api.database
import dbd.core.model.existing
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyModelAccess
import utopia.vault.sql.Extensions._

/**
  * Used for accessing multiple languages at a time
  * @author Mikko Hilpinen
  * @since 10.5.2020, v2
  */
object Languages extends ManyModelAccess[existing.Language]
{
	// IMPLEMENTED	----------------------------
	
	override def factory = database.model.Language
	
	override def globalCondition = None
	
	
	// OTHER	--------------------------------
	
	/**
	  * @param codes ISO-standard language codes
	  * @param connection DB Connection (implicit)
	  * @return Languages that match those codes
	  */
	def forIsoCodes(codes: Set[String])(implicit connection: Connection) = read(Some(
		factory.isoCodeColumn.in(codes)))
}
