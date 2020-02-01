package dbd.client.model.existing

import scala.language.implicitConversions

import dbd.client.model.template.DatabaseSelectionData

object DatabaseSelection
{
	// Implicitly accesses data
	implicit def accessData(selection: DatabaseSelection): DatabaseSelectionData = selection.data
	
	/**
	  * Creates a new model
	  * @param id Id of this recording
	  * @param selectedDatabaseId Id of selected database
	  * @return A new selection model
	  */
	def apply(id: Int, selectedDatabaseId: Int) = new DatabaseSelection(id, DatabaseSelectionData(selectedDatabaseId))
}

/**
  * Represents a recorded database selection event
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
case class DatabaseSelection(id: Int, data: DatabaseSelectionData)
