package dbd.core.model.existing

import dbd.core.model.template.Extender

/**
  * An extender to standard invitation model that also contains the response to that invitation (if present)
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class InvitationWithResponse(invitation: Invitation, response: InvitationResponse) extends Extender[Invitation]
{
	// IMPLEMENTED	------------------------------
	
	override def wrapped = invitation
}
