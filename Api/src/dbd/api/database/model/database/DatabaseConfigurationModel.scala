package dbd.api.database.model.database

import java.time.Instant

import dbd.api.database.Tables
import dbd.api.database.factory.database.DatabaseConfigurationFactory
import dbd.core.model.existing.database
import dbd.core.model.existing.database.DatabaseConfiguration
import dbd.core.model.partial.database.DatabaseConfigurationData
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory

object DatabaseConfigurationModel
{
	// COMPUTED	-------------------------
	
	/**
	  * @return Table used by this model
	  */
	def table = Tables.databaseConfiguration
	
	/**
	  * @return A model that has just been marked as deprecated
	  */
	def nowDeprecated = withDeprecationTime(Instant.now())
	
	
	// OTHER	-------------------------
	
	/**
	  * @param dbId Id of target database
	  * @return A model with database id set
	  */
	def withDatabaseId(dbId: Int) = apply(databaseId = Some(dbId))
	
	/**
	  * @param deprecationTime A deprecation time
	  * @return A model with specified deprecation time set
	  */
	def withDeprecationTime(deprecationTime: Instant) = apply(deprecatedAfter = Some(deprecationTime))
	
	/**
	 * Inserts a new database configuration to the DB
	 * @param databaseId Id of targeted database
	 * @param data Data to insert
	 * @param connection DB connection (implicit)
	 * @return Newly inserted config
	 */
	def insert(databaseId: Int, data: DatabaseConfigurationData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(databaseId), Some(data.name), data.creatorId, Some(Instant.now())).insert().getInt
		DatabaseConfiguration(newId, databaseId, data)
	}
}

/**
 * Used for interacting with database configurations
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class DatabaseConfigurationModel(id: Option[Int] = None, databaseId: Option[Int] = None, name: Option[String] = None,
									  creatorId: Option[Int] = None, created: Option[Instant] = None,
									  deprecatedAfter: Option[Instant] = None)
	extends StorableWithFactory[database.DatabaseConfiguration]
{
	override def factory = DatabaseConfigurationFactory
	
	override def valueProperties = Vector("id" -> id, "databaseId" -> databaseId, "name" -> name, "created" -> created,
		"deprecatedAfter" -> deprecatedAfter, "creatorId" -> creatorId)
}
