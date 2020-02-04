package dbd.core.database

import java.time.Instant

import utopia.vault.sql.Extensions._
import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyModelAccess

/**
  * Used for accessing historical class data from DB
  * @author Mikko Hilpinen
  * @since 4.2.2020, v0.1
  */
object ClassesHistory extends ManyModelAccess[existing.Class]
{
	// IMPLEMENTED	------------------------
	
	override def factory = model.Class
	
	// TODO: Cannot be used for retrieving class contents since the linked items are no longer one-to-one (=> Return just ids?)
	override def globalCondition = None
	
	
	// COMPUTED	----------------------------
	
	/**
	  * @param start Minimum creation time
	  * @param end Maximum creation time
	  * @param connection DB Connection
	  * @return All classes that were created during specified time period
	  */
	def createdBetween(start: Instant, end: Instant)(implicit connection: Connection) =
		find(factory.creationTimeColumn.isBetween(start, end))
	
	/**
	  * @param start Minimum time threshold
	  * @param end Maximum time threshold
	  * @param connection DB Connection (implicit)
	  * @return All classes that were created before specified time period and modified (but not deleted) during it
	  */
	def modifiedBetween(start: Instant, end: Instant)(implicit connection: Connection) =
		find(model.ClassInfo.createdBetweenCondition(start, end) && factory.creationTimeColumn < start &&
			(factory.notDeletedCondition || factory.deletionTimeColumn > end))
	
	/**
	  * @param threshold A time threshold
	  * @param connection DB connection (implicit)
	  * @return All classes that were created before but deleted after the specified instant
	  */
	def deletedAfter(threshold: Instant)(implicit connection: Connection) =
		find(factory.deletionTimeColumn > threshold && factory.creationTimeColumn <= threshold)
	
	/**
	  * @param start Minimum time threshold
	  * @param end Maximum time threshold
	  * @param connection DB connection (implicit)
	  * @return All classes that were created before, but deleted during the specified time period
	  */
	def deletedBetween(start: Instant, end: Instant)(implicit connection: Connection) =
		find(factory.deletionTimeColumn.isBetween(start, end) && factory.creationTimeColumn < start)
}
