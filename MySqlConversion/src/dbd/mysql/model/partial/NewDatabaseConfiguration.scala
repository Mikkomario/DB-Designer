package dbd.mysql.model.partial

import dbd.mysql.model.template.DatabaseConfigurationLike

/**
 * Represents a database configuration before it is saved to DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class NewDatabaseConfiguration(databaseId: Int, name: String) extends DatabaseConfigurationLike
