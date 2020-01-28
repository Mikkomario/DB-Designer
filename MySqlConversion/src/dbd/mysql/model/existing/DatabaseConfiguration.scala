package dbd.mysql.model.existing

import dbd.mysql.model.template.DatabaseConfigurationLike

/**
 * Represents a database configuration stored in DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class DatabaseConfiguration(id: Int, databaseId: Int, name: String) extends DatabaseConfigurationLike
