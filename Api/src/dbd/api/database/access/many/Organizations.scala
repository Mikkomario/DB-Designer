package dbd.api.database.access.many

import dbd.api.database.model.Organization
import utopia.vault.database.Connection

/**
  * Used for accessing multiple organizations at a time
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object Organizations
{
	// COMPUTED	----------------------
	
	private def factory = Organization
	
	
	// OTHER	-----------------------
	
	/**
	  * Inserts a new organization to the database
	  * @param founderId Id of the user who created the organization
	  * @param connection DB Connection (implicit)
	  * @return Id of the newly inserted organization
	  */
	def insert(founderId: Int)(implicit connection: Connection) = factory.insert(founderId)
}
