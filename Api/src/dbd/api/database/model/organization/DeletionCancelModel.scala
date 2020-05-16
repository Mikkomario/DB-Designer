package dbd.api.database.model.organization

import dbd.api.database.Tables
import dbd.api.database.factory.organization.DeletionCancelFactory
import dbd.core.model.existing.organization.DeletionCancel
import dbd.core.model.partial.organization.DeletionCancelData
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory

object DeletionCancelModel
{
	// COMPUTED	-----------------------------
	
	/**
	  * @return Table used by this model
	  */
	def table = Tables.organizationDeletionCancellation
	
	
	// OTHER	-----------------------------
	
	/**
	  * Inserts a new deletion cancel to the DB
	  * @param data Data to insert
	  * @param connection DB Connection
	  * @return Newly inserted cancellation
	  */
	def insert(data: DeletionCancelData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.deletionId), data.creatorId).insert().getInt
		DeletionCancel(newId, data)
	}
}

/**
  * Used for interacting with organization deletion cancellation data
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class DeletionCancelModel(id: Option[Int] = None, deletionId: Option[Int] = None, creatorId: Option[Int] = None)
	extends StorableWithFactory[DeletionCancel]
{
	override def factory = DeletionCancelFactory
	
	override def valueProperties = Vector("id" -> id, "deletionId" -> deletionId, "creatorId" -> creatorId)
}
