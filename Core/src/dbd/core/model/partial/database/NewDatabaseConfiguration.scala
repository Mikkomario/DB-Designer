package dbd.core.model.partial.database

import dbd.core.model.template.DatabaseConfigurationLike

/**
 * Represents a database configuration before it is saved to DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class NewDatabaseConfiguration(name: String) extends DatabaseConfigurationLike
