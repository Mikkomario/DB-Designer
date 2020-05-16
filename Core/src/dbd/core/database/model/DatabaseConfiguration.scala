package dbd.core.database.model

import java.time.Instant

import dbd.core.database.Tables
import dbd.core.model.existing
import dbd.core.model.existing.database
import dbd.core.model.partial.database.NewDatabaseConfiguration
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, RowFactoryWithTimestamps, StorableFactoryWithValidation}

object DatabaseConfiguration extends StorableFactoryWithValidation[database.DatabaseConfiguration] with Deprecatable
	with RowFactoryWithTimestamps[database.DatabaseConfiguration]
{
	// IMPLEMENTED	---------------------
	
	override def table = Tables.databaseConfiguration
	
	override protected def fromValidatedModel(model: Model[Constant]) = database.DatabaseConfiguration(
		model("id").getInt, model("databaseId").getInt, model("name").getString)
	
	override def nonDeprecatedCondition = table("deprecatedAfter").isNull
	
	override def creationTimePropertyName = "created"
	
	
	// COMPUTED	-------------------------
	
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
	def insert(databaseId: Int, data: NewDatabaseConfiguration)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(databaseId), Some(data.name), Some(Instant.now())).insert().getInt
		database.DatabaseConfiguration(newId, databaseId, data.name)
	}
}

/**
 * Used for interacting with database configurations
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class DatabaseConfiguration(id: Option[Int] = None, databaseId: Option[Int] = None, name: Option[String] = None,
								 created: Option[Instant] = None, deprecatedAfter: Option[Instant] = None)
	extends StorableWithFactory[database.DatabaseConfiguration]
{
	override def factory = DatabaseConfiguration
	
	override def valueProperties = Vector("id" -> id, "databaseId" -> databaseId, "name" -> name, "created" -> created,
		"deprecatedAfter" -> deprecatedAfter)
}
