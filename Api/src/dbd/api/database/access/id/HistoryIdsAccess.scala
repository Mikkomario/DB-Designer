package dbd.api.database.access.id

import java.time.Instant

import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Column
import utopia.vault.nosql.access.ManyIdAccess
import utopia.vault.nosql.factory.FromRowFactoryWithTimestamps
import utopia.vault.sql.Condition
import utopia.vault.sql.Extensions._

/**
  * Common trait for id access points that provide access to historical data
  * @author Mikko Hilpinen
  * @since 18.2.2020, v0.1
  */
trait HistoryIdsAccess extends ManyIdAccess[Int]
{
	// ABSTRACT	-----------------------------
	
	def creationTimeColumn: Column
	
	def deletionTimeColumn: Column
	
	def notDeletedCondition: Condition
	
	def configurationFactory: FromRowFactoryWithTimestamps[_]
	
	
	// IMPLEMENTED	-------------------------
	
	override def valueToId(value: Value) = value.int
	
	/**
	  * @param start Minimum creation time
	  * @param end Maximum creation time
	  * @param connection DB Connection
	  * @return All ids of classes that were created during specified time period
	  */
	def createdBetween(start: Instant, end: Instant)(implicit connection: Connection) =
		find(creationTimeColumn.isBetween(start, end))
	
	/**
	  * @param start Minimum time threshold
	  * @param end Maximum time threshold
	  * @param connection DB Connection (implicit)
	  * @return ids of all classes that were created before specified time period and modified (but not deleted) during it
	  */
	def modifiedBetween(start: Instant, end: Instant)(implicit connection: Connection) =
		find(configurationFactory.createdBetweenCondition(start, end) && creationTimeColumn < start &&
			(notDeletedCondition || deletionTimeColumn > end))
	
	/**
	  * @param threshold A time threshold
	  * @param connection DB connection (implicit)
	  * @return Ids of all classes that were created before but deleted after the specified instant
	  */
	def deletedAfter(threshold: Instant)(implicit connection: Connection) =
		find(deletionTimeColumn > threshold && creationTimeColumn <= threshold)
	
	/**
	  * @param start Minimum time threshold
	  * @param end Maximum time threshold
	  * @param connection DB connection (implicit)
	  * @return Ids of all classes that were created before, but deleted during the specified time period
	  */
	def deletedBetween(start: Instant, end: Instant)(implicit connection: Connection) =
		find(deletionTimeColumn.isBetween(start, end) && creationTimeColumn < start)
}
