package dbd.api.database.model.database

import java.time.Instant

import dbd.api.database.Tables
import dbd.api.database.factory.database.DatabaseFactory
import dbd.core.model.existing.database.Database
import dbd.core.model.partial.database.DatabaseData.NewDatabaseData
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory

/**
 * Used for interacting with databases in DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object DatabaseModel
{
	// COMPUTED	-------------------------------
	
	/**
	  * @return Table used by this model
	  */
	def table = Tables.database
	
	
	// OTHER	-------------------------------
	
	/**
	 * Inserts a new database to the DB
	 * @param data New database configuration
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted database
	 */
	def insert(data: NewDatabaseData)(implicit connection: Connection) =
	{
		// Inserts a new database row, then configuration for that database
		val databaseId = apply(None, Some(data.ownerOrganizationId), data.creatorId).insert().getInt
		val newConfig = DatabaseConfigurationModel.insert(databaseId, data.configuration)
		Database(databaseId, data.copy(configuration = newConfig))
	}
}

case class DatabaseModel(id: Option[Int] = None, ownerOrganizationId: Option[Int] = None, creatorId: Option[Int] = None,
						 deletedAfter: Option[Instant] = None) extends StorableWithFactory[Database]
{
	override def factory = DatabaseFactory
	
	override def valueProperties = Vector("id" -> id, "ownerId" -> ownerOrganizationId, "creatorId" -> creatorId,
		"deletedAfter" -> deletedAfter)
}
