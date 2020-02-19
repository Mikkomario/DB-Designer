package dbd.client.controller

import dbd.client.model.{ChangedItems, DisplayedRelease}
import dbd.core.database.Classes
import dbd.mysql.database.Releases
import utopia.vault.database.Connection

/**
  * Used for reading release data that will then be displayed
  * @author Mikko Hilpinen
  * @since 19.2.2020, v0.1
  */
object ReadReleaseData
{
	/**
	  * Reads the displayed changes between releases
	  * @param databaseId Id of targeted database
	  * @param connection DB Connection
	  * @return Latest releases for specified database + changes included in them
	  */
	def forDatabaseWithId(databaseId: Int)(implicit connection: Connection) =
	{
		val classAccess = Classes.inDatabaseWithId(databaseId)
		
		// Finds the latest release (TODO: Later add more releases)
		Releases.forDatabaseWithId(databaseId).latest match
		{
			case Some(latestRelease) =>
				val latestReleaseTime = latestRelease.released
				
				// Finds the changes since the latest release
				val newClasses = classAccess.createdAfter(latestReleaseTime)
				val modifiedClasses = classAccess.modifiedAfter(latestReleaseTime)
				val deletedClasses = classAccess.deleted.after(latestReleaseTime)
				
				val lastReleaseDisplay = DisplayedRelease(Some(latestRelease), ChangedItems.empty, ChangedItems.empty, ChangedItems.empty)
				
				if (newClasses.nonEmpty || modifiedClasses.nonEmpty || deletedClasses.nonEmpty)
					Vector(
						DisplayedRelease(None, ChangedItems(newClasses, Map(), Vector()),
							ChangedItems(modifiedClasses, Map(), Vector()), ChangedItems(deletedClasses, Map(), Vector())),
						lastReleaseDisplay)
				else
					Vector(lastReleaseDisplay)
				
			case None =>
				val newClasses = classAccess.all
				if (newClasses.nonEmpty)
					Vector(DisplayedRelease(None, ChangedItems(newClasses, Map(), Vector()), ChangedItems.empty, ChangedItems.empty))
				else
					Vector()
		}
	}
}
