package dbd.client.controller

import dbd.core.database.{ConnectionPool, Databases}
import dbd.core.model.existing.{Database, DatabaseConfiguration}
import dbd.core.model.partial.NewDatabaseConfiguration
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
				// TODO: Read last selected db
				databases -> databases.head
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
}
