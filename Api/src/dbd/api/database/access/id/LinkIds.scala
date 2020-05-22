package dbd.api.database.access.id

import dbd.api.database.model.database.{LinkModel, LinkConfigurationModel}
import utopia.vault.sql.Condition

/**
  * Used for accessing link ids
  * @author Mikko Hilpinen
  * @since 18.2.2020, v0.1
  */
object LinkIds extends LinkIdsAccess(None)
{
	/**
	  * @param databaseId Id of target database
	  * @return An access point to link ids in that database
	  */
	def inDatabaseWithId(databaseId: Int) = new LinkIdsAccess(Some(factory.withDatabaseId(databaseId).toCondition))
}

class LinkIdsAccess(override val globalCondition: Option[Condition]) extends HistoryIdsAccess
{
	def factory = LinkModel
	
	override def configurationFactory = LinkConfigurationModel
	
	override def creationTimeColumn = factory.creationTimeColumn
	
	override def deletionTimeColumn = factory.deletedAfterColumn
	
	override def notDeletedCondition = factory.notDeletedCondition
	
	override def target = factory.target
	
	override def table = factory.table
}