package dbd.core.database

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import utopia.vault.sql.Extensions._
import dbd.core.model.existing
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyModelAccess

/**
  * An access point to classes that have been deleted
  * @author Mikko Hilpinen
  * @since 19.2.2020, v0.1
  */
object DeletedClasses extends ManyModelAccess[existing.Class]
{
	// IMPLEMENTED	------------------------------
	
	override def factory = model.Class
	
	override def globalCondition = Some(factory.nonDeprecatedDataCondition && factory.deletionTimeColumn.isNotNull)
	
	
	// OTHER	---------------------------------
	
	/**
	  * @param databaseId Id of target database
	  * @return An access point to deleted classes in that database
	  */
	def inDatabaseWithId(databaseId: Int) = new DeletedClassesInDatabase(databaseId)
	
	
	// NESTED	---------------------------------
	
	class DeletedClassesInDatabase(databaseId: Int) extends ManyModelAccess[existing.Class]
	{
		// IMPLEMENTED	-------------------------
		
		override def factory = DeletedClasses.this.factory
		
		override def globalCondition =
			Some(DeletedClasses.this.mergeCondition(factory.withDatabaseId(databaseId).toCondition))
		
		
		// OTHER	------------------------------
		
		/**
		  * @param threshold A time threshold
		  * @param connection DB Connection
		  * @return All classes that were deleted after the specified time threshold
		  */
		def after(threshold: Instant)(implicit connection: Connection) = find(factory.deletionTimeColumn > threshold)
	}
}
