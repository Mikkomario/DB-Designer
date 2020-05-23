package dbd.core.model.partial.database

import java.time.Instant

import dbd.core.model.existing.database.DatabaseConfiguration
import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

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
case class DatabaseData[+Config <: ModelConvertible](ownerOrganizationId: Int, configuration: Config,
													 creatorId: Option[Int] = None, created: Instant = Instant.now())
	extends ModelConvertible
{
	override def toModel = Model(Vector("owner_organization_id" -> ownerOrganizationId,
		"configuration" -> configuration.toModel, "created" -> created, "creator_id" -> creatorId))
}
