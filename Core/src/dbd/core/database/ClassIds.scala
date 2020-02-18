package dbd.core.database

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import utopia.vault.sql.Extensions._
import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyIdAccess

/**
  * Used for accessing multiple class ids at a time
  * @author Mikko Hilpinen
  * @since 18.2.2020, v0.1
  */
object ClassIds extends ManyIdAccess[Int]
{
	// IMPLEMENTED	---------------------------
	
	override def target = factory.target
	
	override def valueToId(value: Value) = value.int
	
	override def table = factory.table
	
	override def globalCondition = None
	
	
	// COMPUTED	-------------------------------
	
	def factory = model.Class
	
	
	// OTHER	-------------------------------
	
	/**
	  * @param start Minimum creation time
	  * @param end Maximum creation time
	  * @param connection DB Connection
	  * @return All ids of classes that were created during specified time period
	  */
	def ofClassesCreatedBetween(start: Instant, end: Instant)(implicit connection: Connection) =
		find(factory.creationTimeColumn.isBetween(start, end))
	
	/**
	  * @param start Minimum time threshold
	  * @param end Maximum time threshold
	  * @param connection DB Connection (implicit)
	  * @return ids of all classes that were created before specified time period and modified (but not deleted) during it
	  */
	def ofClassesModifiedBetween(start: Instant, end: Instant)(implicit connection: Connection) =
		find(model.ClassInfo.createdBetweenCondition(start, end) && factory.creationTimeColumn < start &&
			(factory.notDeletedCondition || factory.deletionTimeColumn > end))
	
	/**
	  * @param threshold A time threshold
	  * @param connection DB connection (implicit)
	  * @return Ids of all classes that were created before but deleted after the specified instant
	  */
	def ofClassesDeletedAfter(threshold: Instant)(implicit connection: Connection) =
		find(factory.deletionTimeColumn > threshold && factory.creationTimeColumn <= threshold)
	
	/**
	  * @param start Minimum time threshold
	  * @param end Maximum time threshold
	  * @param connection DB connection (implicit)
	  * @return Ids of all classes that were created before, but deleted during the specified time period
	  */
	def ofClassesDeletedBetween(start: Instant, end: Instant)(implicit connection: Connection) =
		find(factory.deletionTimeColumn.isBetween(start, end) && factory.creationTimeColumn < start)
}
