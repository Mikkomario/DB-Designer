package dbd.client.database.model

import java.time.Instant

/**
  * Used for interacting with database selections in DB
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
case class DatabaseSelection(id: Option[Int] = None, selectedDatabaseId: Option[Int] = None,
							 created: Option[Instant] = None)
