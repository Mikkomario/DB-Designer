package dbd.core.model.existing.database

import dbd.core.model.existing.Stored
import dbd.core.model.partial.database.DatabaseConfigurationData

/**
 * Represents a database configuration stored in DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class DatabaseConfiguration(id: Int, databaseId: Int, data: DatabaseConfigurationData)
	extends Stored[DatabaseConfigurationData]
