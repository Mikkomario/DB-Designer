package dbd.api.database.model

import dbd.core.model.existing
import utopia.vault.model.immutable.Row
import utopia.vault.nosql.factory.FromRowFactory

/**
  * Used for reading invitation data, including response data
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
object InvitationWithResponse extends FromRowFactory[existing.InvitationWithResponse]
{
	// IMPLEMENTED	------------------------------
	
	override def apply(row: Row) = Invitation(row).flatMap { invitation =>
		InvitationResponse(row).map { response => existing.InvitationWithResponse(invitation, response) }
	}
	
	override def table = Invitation.table
	
	override def joinedTables = InvitationResponse.tables
}
