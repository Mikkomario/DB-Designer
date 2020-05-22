package dbd.api.database.access.many.database

import java.time.Instant

import dbd.api.database.model
import dbd.core.model.existing.database
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyModelAccess
import utopia.vault.sql.Extensions._

/**
  * An access point to classes that have been deleted
  * @author Mikko Hilpinen
  * @since 19.2.2020, v0.1
  */
object DbDeletedClasses extends ManyModelAccess[database.Class]
{
	// IMPLEMENTED	------------------------------
	
	override def factory = model.database.ClassModel
	
	override def globalCondition = Some(factory.nonDeprecatedDataCondition && factory.deletionTimeColumn.isNotNull)
	
	
	// OTHER	---------------------------------
	
	/**
	  * @param databaseId Id of target database
	  * @return An access point to deleted classes in that database
	  */
	def inDatabaseWithId(databaseId: Int) = new DeletedClassesInDatabase(databaseId)
	
	
	// NESTED	---------------------------------
	
	class DeletedClassesInDatabase(databaseId: Int) extends ManyModelAccess[database.Class]
	{
		// IMPLEMENTED	-------------------------
		
		override def factory = DbDeletedClasses.this.factory
		
		override def globalCondition =
			Some(DbDeletedClasses.this.mergeCondition(factory.withDatabaseId(databaseId).toCondition))
		
		
		// OTHER	------------------------------
		
		/**
		  * @param threshold A time threshold
		  * @param connection DB Connection
		  * @return All classes that were deleted after the specified time threshold
		  */
		def after(threshold: Instant)(implicit connection: Connection) = find(factory.deletionTimeColumn > threshold)
	}
}
