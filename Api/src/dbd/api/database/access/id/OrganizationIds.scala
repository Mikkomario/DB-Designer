package dbd.api.database.access.id

import dbd.api.database.model.Organization
import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyIdAccess

/**
  * Used for accessing multiple organization ids at a time
  * @author Mikko Hilpinen
  * @since 13.5.2020, v2
  */
object OrganizationIds extends ManyIdAccess[Int]
{
	// IMPLEMENTED	---------------------
	
	override def target = factory.table
	
	override def valueToId(value: Value) = value.int
	
	override def table = factory.table
	
	override def globalCondition = None
	
	
	// COMPUTED	------------------------
	
	private def factory = Organization
	
	/**
	  * @param connection DB Connection (implicit)
	  * @return Ids of organizations that are waiting to be deleted / have been marked as deleted
	  */
	def waitingDeletion(implicit connection: Connection) = find(factory.deletedCondition)
}
