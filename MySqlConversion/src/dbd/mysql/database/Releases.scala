package dbd.mysql.database

import dbd.mysql.model.existing
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyModelAccess

/**
  * Used for accessing multiple releases at a time
  * @author Mikko Hilpinen
  * @since 19.2.2020, v0.1
  */
object Releases extends ManyModelAccess[existing.Release]
{
	// IMPLEMENTED	-------------------------
	
	override def factory = model.Release
	
	override def globalCondition = None
	
	
	// OTHER	-----------------------------
	
	/**
	  * @param databaseId Id of targeted database
	  * @return An access point to releases for that database
	  */
	def forDatabaseWithId(databaseId: Int) = new ReleasesForDatabase(databaseId)
	
	
	// NESTED	-----------------------------
	
	class ReleasesForDatabase(databaseId: Int) extends ManyModelAccess[existing.Release]
	{
		// IMPLEMENTED	---------------------
		
		override def factory = Releases.this.factory
		
		override def globalCondition = Some(Releases.this.mergeCondition(
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
		def takeLatest(numberOfReleases: Int)(implicit connection: Connection) = factory.takeLatest(numberOfReleases)
	}
}
