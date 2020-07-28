package dbd.client.controller

import dbd.api.database.ConnectionPool
import dbd.api.database.access.many.database.DbDatabases
import dbd.api.database.access.single.database.DbDatabase
import utopia.flow.util.CollectionExtensions._
import dbd.client.database.DatabaseSelection
import dbd.client.model.template.DatabaseSelectionData
import dbd.core.model.existing.database.{Database, DatabaseConfiguration}
import dbd.core.model.partial.database.DatabaseData.NewDatabaseData
import dbd.core.model.partial.database.{DatabaseConfigurationData, DatabaseData}
import dbd.core.util.Log
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.component.template.input.SelectableWithPointers

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Manages the currently displayed & selected databases
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
@deprecated("This class will be replaced with an api-interface. Currently out of order.", "v2")
class DatabasesManager(implicit exc: ExecutionContext) extends SelectableWithPointers[Database, Vector[Database]]
{
	// ATTRIBUTES	-----------------------
	
	override val (contentPointer, valuePointer) =
	{
		// Reads all databases from the DB
		ConnectionPool.tryWith { implicit connection =>
			val databases = DbDatabases.all
			// If there are no databases in DB, has to insert one
			if (databases.isEmpty)
			{
				// FIXME: Should have a creator id and owner organization id set (this will be moved to api side anyway)
				val newDB = DbDatabases.insert(DatabaseData(???, DatabaseConfigurationData("my_first_db")))
				Vector(newDB) -> newDB
			}
			else
			{
				// Checks which database was last selected (or configured)
				val latestDB = DatabaseSelection.latest.flatMap { s => databases.find {
					_.id == s.selectedDatabaseId } }.orElse { DbDatabase.lastConfigured }.getOrElse(databases.head)
				
				databases -> latestDB
			}
		}
		match
		{
			case Success(data) => new PointerWithEvents[Vector[Database]](data._1) -> new PointerWithEvents(data._2)
			case Failure(error) =>
				Log(error, "Failed to read databases from DB")
				// FIXME: This is probably not a valid way of handling this problem. Resolve when moving to api
				new PointerWithEvents[Vector[Database]](Vector()) -> new PointerWithEvents(Database(-1, DatabaseData(-1,
					DatabaseConfiguration(-1, -1, DatabaseConfigurationData("Failed to Load")))))
		}
	}
	
	
	// INITIAL CODE	----------------------
	
	// Each time database selection changes, records it in the database
	valuePointer.addListener { e =>
		ConnectionPool.tryWith { implicit connection =>
			DatabaseSelection.insert(DatabaseSelectionData(e.newValue.id))
		}.failure.foreach { Log(_, s"Couldn't record a database change ($e)") }
	}
	
	
	// OTHER	--------------------------
	
	/**
	  * Adds a new database to the DB
	  * @param newDB New database data
	  */
	def addNewDatabase(newDB: NewDatabaseData) =
	{
		ConnectionPool.tryWith { implicit connection => DbDatabases.insert(newDB) } match
		{
			case Success(newDB) =>
				content :+= newDB
				select(newDB)
			case Failure(error) => Log(error, s"Failed to insert a new database with data: $newDB")
		}
	}
	
	/**
	  * Edits the configuration of an existing database
	  * @param databaseId Id of target database
	  * @param newConfig New configuration for the database
	  */
	def editDatabase(databaseId: Int, newConfig: DatabaseConfigurationData) =
	{
		ConnectionPool.tryWith { implicit connection => DbDatabase(databaseId).configuration.update(newConfig) } match {
			case Success(savedConfig) =>
				content = content.mapFirstWhere { _.id == databaseId } { _.withConfiguration(savedConfig) }
			case Failure(error) => Log(error, "Failed to edit database configuration")
		}
	}
}
