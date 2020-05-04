package dbd.core.model.existing

import dbd.core.model.partial.InvitationResponseData

/**
  * Represents an invitation response that has been stored to DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class InvitationResponse(id: Int, data: InvitationResponseData) extends Stored[InvitationResponseData]
