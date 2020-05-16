package dbd.client.controller

import utopia.flow.util.CollectionExtensions._
import dbd.client.database.DatabaseSelection
import dbd.client.model.template.DatabaseSelectionData
import dbd.core.database
import dbd.core.database.{ConnectionPool, Databases}
import dbd.core.model.existing.database.{Database, DatabaseConfiguration}
import dbd.core.model.partial.database.NewDatabaseConfiguration
import dbd.core.util.Log
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.component.input.SelectableWithPointers

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Manages the currently displayed & selected databases
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class DatabasesManager(implicit exc: ExecutionContext) extends SelectableWithPointers[Database, Vector[Database]]
{
	// ATTRIBUTES	-----------------------
	
	override val (contentPointer, valuePointer) =
	{
		// Reads all databases from the DB
		ConnectionPool.tryWith { implicit connection =>
			val databases = Databases.all
			// If there are no databases in DB, has to insert one
			if (databases.isEmpty)
			{
				val newDB = Databases.insert(NewDatabaseConfiguration("my_first_db"))
				Vector(newDB) -> newDB
			}
			else
			{
				// Checks which database was last selected (or configured)
				val latestDB = DatabaseSelection.latest.flatMap { s => databases.find {
					_.id == s.selectedDatabaseId } }.orElse { database.Database.lastConfigured }.getOrElse(databases.head)
				
				databases -> latestDB
			}
		}
		match
		{
			case Success(data) => new PointerWithEvents[Vector[Database]](data._1) -> new PointerWithEvents(data._2)
			case Failure(error) =>
				Log(error, "Failed to read databases from DB")
				new PointerWithEvents[Vector[Database]](Vector()) -> new PointerWithEvents(Database(-1,
					DatabaseConfiguration(-1, -1, "Failed to Load")))
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
	  * @param newConfig First configuration for the new DB
	  */
	def addNewDatabase(newConfig: NewDatabaseConfiguration) =
	{
		ConnectionPool.tryWith { implicit connection => Databases.insert(newConfig) } match
		{
			case Success(newDB) =>
				content :+= newDB
				select(newDB)
			case Failure(error) => Log(error, s"Failed to insert a new database with config: $newConfig")
		}
	}
	
	/**
	  * Edits the configuration of an existing database
	  * @param databaseId Id of target database
	  * @param newConfig New configuration for the database
	  */
	def editDatabase(databaseId: Int, newConfig: NewDatabaseConfiguration) =
	{
		ConnectionPool.tryWith { implicit connection => database.Database(databaseId).configuration.update(newConfig) } match {
			case Success(savedConfig) =>
				content = content.mapFirstWhere { _.id == databaseId } { _.copy(configuration = savedConfig) }
			case Failure(error) => Log(error, "Failed to edit database configuration")
		}
	}
}
