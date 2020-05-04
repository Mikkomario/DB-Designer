package dbd.core.model.existing

import dbd.core.model.partial.InvitationData

/**
  * Represents an organization invitation that has been stored to the DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class Invitation(id: Int, data: InvitationData) extends Stored[InvitationData]
