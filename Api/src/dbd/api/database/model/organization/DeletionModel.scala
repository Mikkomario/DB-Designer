package dbd.api.database.model.organization

import java.time.Instant

import dbd.api.database.factory.organization.DeletionFactory
import dbd.core.model.existing.organization.Deletion
import dbd.core.model.partial.organization.DeletionData
import utopia.vault.model.immutable.Storable
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection

object DeletionModel
{
	/**
	  * @param organizationId Id of targeted organization
	  * @return A model with only organization id set
	  */
	def withOrganizationId(organizationId: Int) = apply(organizationId = Some(organizationId))
	
	/**
	  * @param actualization Actualization time
	  * @return A model with only actualization time set
	  */
	def withActualizationTime(actualization: Instant) = apply(actualizationTime = Some(actualization))
	
	/**
	  * Inserts a new organization deletion attempt to DB
	  * @param data Data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted deletion
	  */
	def insert(data: DeletionData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.organizationId), Some(data.creatorId),
			Some(data.actualizationTime)).insert().getInt
		Deletion(newId, data)
	}
}

/**
  * Used for interacting with organization deletions in DB
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class DeletionModel(id: Option[Int] = None, organizationId: Option[Int] = None, creatorId: Option[Int] = None,
						 actualizationTime: Option[Instant] = None) extends Storable
{
	override def table = DeletionFactory.table
	
	override def valueProperties = Vector("id" -> id, "organizationId" -> organizationId, "creatorId" -> creatorId,
		"actualization" -> actualizationTime)
}