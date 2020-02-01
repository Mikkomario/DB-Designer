package dbd.client.database.model

import java.time.Instant

import dbd.client.database.Tables
import utopia.flow.generic.ValueConversions._
import dbd.client.model.existing
import dbd.client.model.template.DatabaseSelectionData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{RowFactoryWithTimestamps, StorableFactoryWithValidation}

object DatabaseSelection extends StorableFactoryWithValidation[existing.DatabaseSelection]
	with RowFactoryWithTimestamps[existing.DatabaseSelection]
{
	// IMPLEMENTED	------------------------
	
	override def table = Tables.databaseSelection
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.DatabaseSelection(
		model("id").getInt, model("selectedDatabaseId").getInt)
	
	override def creationTimePropertyName = "created"
	
	
	// OTHER	----------------------------
	
	/**
	  * Inserts a new database selection to the DB
	  * @param selection selection data
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted selection
	  */
	def insert(selection: DatabaseSelectionData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(selection.selectedDatabaseId)).insert().getInt
		existing.DatabaseSelection(newId, selection)
	}
}

/**
  * Used for interacting with database selections in DB
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
case class DatabaseSelection(id: Option[Int] = None, selectedDatabaseId: Option[Int] = None,
							 created: Option[Instant] = None) extends StorableWithFactory[existing.DatabaseSelection]
{
	override def factory = DatabaseSelection
	
	override def valueProperties = Vector("id" -> id, "selectedDatabaseId" -> selectedDatabaseId, "created" -> created)
}
