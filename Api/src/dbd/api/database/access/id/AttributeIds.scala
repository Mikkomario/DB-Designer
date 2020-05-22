package dbd.api.database.access.id

import dbd.api.database.model.database
import dbd.api.database.model.database.{AttributeModel, AttributeConfigurationModel}
import utopia.vault.model.immutable.Table
import utopia.vault.sql.Condition

/**
  * Used for accessing multiple attribute ids at a time
  * @author Mikko Hilpinen
  * @since 18.2.2020, v0.1
  */
object AttributeIds extends AttributeIdsAccess(None, None)
{
	/**
	  * @param databaseId Id of targeted database
	  * @return An access point to attribute ids within that database
	  */
	def inDatabaseWithId(databaseId: Int) = new AttributeIdsAccess(
		Some(database.ClassModel.withDatabaseId(databaseId).toCondition), Some(database.ClassModel.table))
}

class AttributeIdsAccess(override val globalCondition: Option[Condition], joinedTable: Option[Table]) extends HistoryIdsAccess
{
	def factory = AttributeModel
	
	override def creationTimeColumn = factory.creationTimeColumn
	
	override def deletionTimeColumn = factory.deletedAfterColumn
	
	override def notDeletedCondition = factory.notDeletedCondition
	
	override def configurationFactory = AttributeConfigurationModel
	
	override def target = joinedTable.map { factory.target join _ }.getOrElse(factory.target)
	
	override def table = factory.table
}