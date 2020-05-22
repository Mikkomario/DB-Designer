package dbd.api.database.access.many.database

import java.time.Instant

import dbd.api.database.model.mysql.ReleaseModel
import dbd.core.model.existing.mysql.Release
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{ManyIdAccess, ManyModelAccess}

/**
  * Used for accessing multiple releases at a time
  * @author Mikko Hilpinen
  * @since 19.2.2020, v0.1
  */
object DbReleases extends ManyModelAccess[Release]
{
	// IMPLEMENTED	-------------------------
	
	override val factory = ReleaseModel
	
	override val globalCondition = None
	
	
	// COMPUTED	-----------------------------
	
	/**
	  * An access point to release ids
	  */
	val ids = ManyIdAccess.wrap(factory) { _.int }
	
	
	// OTHER	-----------------------------
	
	/**
	  * @param databaseId Id of targeted database
	  * @return An access point to releases for that database
	  */
	def forDatabaseWithId(databaseId: Int) = new ReleasesForDatabase(databaseId)
	
	
	// NESTED	-----------------------------
	
	class ReleasesForDatabase(databaseId: Int) extends ManyModelAccess[Release]
	{
		// IMPLEMENTED	---------------------
		
		override def factory = DbReleases.this.factory
		
		override val globalCondition = Some(DbReleases.this.mergeCondition(
			factory.withDatabaseId(databaseId).toCondition))
		
		
		// COMPUTED	-------------------------
		
		/**
		  * @param connection DB Connection (implicit)
		  * @return Latest release for this database
		  */
		def latest(implicit connection: Connection) = factory.findLatest(globalCondition.get)
		
		
		// OTHER	-------------------------
		
		/**
		  * @param numberOfReleases Maximum number of releases returned
		  * @param connection DB Connection
		  * @return Up to 'numberOfReleases' latest releases from the database
		  */
		def takeLatest(numberOfReleases: Int)(implicit connection: Connection) =
			factory.takeLatestWhere(globalCondition.get, numberOfReleases)
		
		/**
		  * Finds a number of releases before specified timestamp
		  * @param threshold Time threshold (exclusive)
		  * @param maxNumberOfResults Maximum number of releases returned (default = 10)
		  * @param connection DB Connection (implicit)
		  * @return Up to 'maxNumberOfResults' releases in this database before specified timestamp
		  */
		def before(threshold: Instant, maxNumberOfResults: Int = 10)(implicit connection: Connection) =
			factory.createdBefore(threshold, maxNumberOfResults, additionalCondition = globalCondition)
	}
}
