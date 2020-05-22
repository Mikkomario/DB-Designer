package dbd.api.database.access.id

import dbd.api.database.model.database.{ClassModel, ClassInfoModel}
import utopia.vault.sql.Condition

/**
  * Used for accessing multiple class ids at a time
  * @author Mikko Hilpinen
  * @since 18.2.2020, v0.1
  */
object ClassIds extends ClassIdsAccess(None)
{
	/**
	  * @param databaseId Id of targeted database
	  * @return A subgroup of these ids in specified database
	  */
	def inDatabaseWithId(databaseId: Int) = new ClassIdsAccess(Some(factory.withDatabaseId(databaseId).toCondition))
}

class ClassIdsAccess(override val globalCondition: Option[Condition]) extends HistoryIdsAccess
{
	// IMPLEMENTED	---------------------------
	
	override def configurationFactory = ClassInfoModel
	
	override def creationTimeColumn = factory.creationTimeColumn
	
	override def deletionTimeColumn = factory.deletionTimeColumn
	
	override def notDeletedCondition = factory.notDeletedCondition
	
	override def target = factory.target
	
	override def table = factory.table
	
	
	// COMPUTED	-------------------------------
	
	def factory = ClassModel
}
