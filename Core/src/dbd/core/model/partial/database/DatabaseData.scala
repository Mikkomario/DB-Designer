package dbd.core.model.partial.database

import dbd.core.model.existing.database.DatabaseConfiguration

object DatabaseData
{
	/**
	  * Type of partial database data elements
	  */
	type NewDatabaseData = DatabaseData[DatabaseConfigurationData]
	
	/**
	  * Type of existing database data elements
	  */
	type ExistingDatabaseData = DatabaseData[DatabaseConfiguration]
}

/**
  * Contains basic data about a database
  * @author Mikko Hilpinen
  * @since 23.5.2020, v2
  */
case class DatabaseData[+Config](ownerOrganizationId: Int, configuration: Config, creatorId: Option[Int] = None)
