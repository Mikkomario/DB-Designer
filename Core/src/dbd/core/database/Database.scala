package dbd.core.database

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.partial.NewDatabaseConfiguration
import utopia.vault.database.Connection
import utopia.vault.model.enumeration.ComparisonOperator.Larger
import utopia.vault.nosql.access.{NonDeprecatedAccess, SingleIdModelAccess, SingleModelAccess, UniqueAccess}
import utopia.vault.sql.Where

/**
 * An access point for individual databases
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object Database extends SingleModelAccess[existing.Database] with NonDeprecatedAccess[existing.Database, Option[existing.Database]]
{
	// IMPLEMENTED	-------------------
	
	override def factory = model.Database
	
	
	// COMPUTED	-----------------------
	
	/**
	  * @param connection Implicit DB connection
	  * @return The last configured database from the DB
	  */
	def lastConfigured(implicit connection: Connection) = first(model.DatabaseConfiguration.defaultOrdering)
	
	
	// OTHER	-----------------------
	
	/**
	 * @param id Id of targeted database
	 * @return An access point to that database's data
	 */
	def apply(id: Int) = new SingleDatabase(id)
	
	
	// NESTED	-----------------------
	
	/**
	 * Used for accessing a single database's data
	 * @param databaseId Id of targeted database
	 */
	class SingleDatabase(databaseId: Int) extends SingleIdModelAccess[existing.Database](databaseId, Database.this.factory)
	{
		// COMPUTED	--------------------
		
		/**
		 * @return An access point to this database's classes
		 */
		def classes = Classes.inDatabaseWithId(databaseId)
		
		/**
		 * @return An access point to this database's links
		 */
		def links = Links.inDatabaseWithId(databaseId)
		
		/**
		  * @return An access point to this database's current configuration
		  */
		def configuration = Configuration
		
		
		// NESTED	--------------------
		
		/**
		  * Accesses an individual database's current configuration
		  */
		object Configuration extends SingleModelAccess[existing.DatabaseConfiguration]
			with UniqueAccess[existing.DatabaseConfiguration]
		{
			// IMPLEMENTED	------------
			
			override def factory = model.DatabaseConfiguration
			
			override val condition = factory.withDatabaseId(databaseId).toCondition && factory.nonDeprecatedCondition
			
			
			// OTHER	----------------
			
			/**
			  * @param time A specific time
			  * @return An access point to this database's configuration at that specific time
			  */
			def during(time: Instant) = new ConfigurationHistory(time)
			
			/**
			  * Updates a database's current configuration
			  * @param newData New configuration
			  * @param connection DB Connection
			  * @return Newly inserted configuration
			  */
			def update(newData: NewDatabaseConfiguration)(implicit connection: Connection) =
			{
				// Deprecates the old version and inserts the new one
				connection(factory.nowDeprecated.toUpdateStatement() + globalCondition.map { Where(_) })
				factory.insert(databaseId, newData)
			}
		}
		
		/**
		  * Access an individual database's configuration at certain time point
		  * @param readTime Targeted time point
		  */
		class ConfigurationHistory(readTime: Instant) extends SingleModelAccess[existing.DatabaseConfiguration]
			with UniqueAccess[existing.DatabaseConfiguration]
		{
			// IMPLEMENTED	--------------
			
			override val condition = factory.withDatabaseId(databaseId).toCondition && (
				factory.createdBeforeCondition(readTime, isInclusive = true),
				factory.nonDeprecatedCondition || factory.withDeprecationTime(readTime).toConditionWithOperator(Larger))
			
			override def factory = model.DatabaseConfiguration
		}
	}
}
