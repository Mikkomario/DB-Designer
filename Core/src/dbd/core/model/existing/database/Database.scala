package dbd.core.model.existing.database

import dbd.core.model.existing.StoredModelConvertible
import dbd.core.model.partial.database.DatabaseData

/**
 * Represents a database in DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class Database(id: Int, data: DatabaseData[DatabaseConfiguration])
	extends StoredModelConvertible[DatabaseData[DatabaseConfiguration]]
{
	/**
	  * @param newConfiguration A new configuration for this database
	  * @return A copy of this database with the new configuration
	  */
	def withConfiguration(newConfiguration: DatabaseConfiguration) = copy(data = data.copy(configuration = newConfiguration))
}
