package dbd.core.database

import java.time.Instant

import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.ValueConversions._
import utopia.vault.sql.Extensions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Column
import utopia.vault.nosql.access.ManyModelAccess
import utopia.vault.nosql.factory.FromRowFactoryWithTimestamps

/**
  * Used for accessing multiple models and specifically those that have been added or modified
  * @author Mikko Hilpinen
  * @since 19.2.2020, v0.1
  */
trait ChangedModelsAccess[+A, +Conf] extends ManyModelAccess[A]
{
	// ABSTRACT	--------------------------
	
	def creationTimeColumn: Column
	
	/**
	  * @return Factory used for retrieving only current data configurations
	  */
	def configurationFactory: FromRowFactoryWithTimestamps[Conf]
	
	
	// OTHER	--------------------------
	
	/**
	  * @param threshold A time threshold
	  * @param connection DB Connection
	  * @return All new, non-deleted classes that were created after the specified time threshold
	  */
	def createdAfter(threshold: Instant)(implicit connection: Connection) =
		find(creationTimeColumn > threshold)
	
	/**
	  * @param threshold A time threshold
	  * @param connection DB Connection
	  * @return All non-deleted classes that were created before the specified instant but were modified after that
	  */
	def modifiedAfter(threshold: Instant)(implicit connection: Connection) =
		find(configurationFactory.createdAfterCondition(threshold) && creationTimeColumn <= threshold)
	
	/**
	  * @param ids Row ids of desired items
	  * @param connection DB Connection (implicit)
	  * @return All models with specified ids
	  */
	def withIds(ids: IterableOnce[Int])(implicit connection: Connection) = factory.withIds(ids.iterator.map { i => i: Value })
}
