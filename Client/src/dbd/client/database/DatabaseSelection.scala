package dbd.client.database

import utopia.flow.generic.ValueConversions._
import dbd.client.model.existing
import dbd.client.model.template.DatabaseSelectionData
import utopia.vault.database.Connection
import utopia.vault.nosql.access.SingleModelAccessById

/**
  * Used for accessing individual database selection events
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
object DatabaseSelection extends SingleModelAccessById[existing.DatabaseSelection, Int]
{
	// IMPLEMENTED	----------------------------
	
	override def idToValue(id: Int) = id
	
	override def factory = model.DatabaseSelection
	
	
	// COMPUTED	--------------------------------
	
	/**
	  * @param connection DB Connection (implicit)
	  * @return Latest recorded database selection
	  */
	def latest(implicit connection: Connection) = factory.latest
	
	
	// OTHER	--------------------------------
	
	/**
	  * Records a new database selection
	  * @param newSelection Data of new selection
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted model
	  */
	def insert(newSelection: DatabaseSelectionData)(implicit connection: Connection) = factory.insert(newSelection)
}
