package dbd.mysql.database

import utopia.flow.generic.ValueConversions._
import dbd.mysql.model.existing
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleIdModelAccess, SingleModelAccessById}

/**
 * Used for interacting with individual releases
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object Release extends SingleModelAccessById[existing.Release, Int]
{
	// IMPLEMENTED	------------------------
	
	override def idToValue(id: Int) = id
	
	override def factory = model.Release
	
	override def apply(id: Int) = new SingleRelease(id)
	
	
	// OTHER	----------------------------
	
	/**
	 * @param connection DB Connection (implicit)
	 * @return The latest release instance
	 */
	def latest(implicit connection: Connection) = factory.latest
	
	
	// NESTED	----------------------------
	
	/**
	 * Accesses an individual release's data
	 * @param releaseId Id of target release
	 */
	class SingleRelease(releaseId: Int) extends SingleIdModelAccess[existing.Release](releaseId, Release.this.factory)
	{
		// COMPUTED	------------------------
		
		/**
		 * @param connection DB Connection
		 * @return All table data associated with this release
		 */
		def tables(implicit connection: Connection) = model.Table.getMany(model.Table.withReleaseId(releaseId).toCondition)
	}
}
