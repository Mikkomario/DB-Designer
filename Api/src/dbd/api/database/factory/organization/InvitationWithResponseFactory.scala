package dbd.api.database.factory.organization

import dbd.api.database.model.organization.{InvitationModel, InvitationResponseModel}
import dbd.core.model.combined.organization
import utopia.vault.model.immutable.Row
import utopia.vault.nosql.factory.FromRowFactory

/**
  * Used for reading invitation data, including response data
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object InvitationWithResponseFactory extends FromRowFactory[organization.InvitationWithResponse]
{
	// IMPLEMENTED	------------------------------
	
	override def apply(row: Row) = InvitationModel(row).flatMap { invitation =>
		InvitationResponseModel(row).map { response => organization.InvitationWithResponse(invitation, response) }
	}
	
	override def table = InvitationModel.table
	
	override def joinedTables = InvitationResponseModel.tables
}
