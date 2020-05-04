package dbd.core.model.partial

/**
  * Contains basic data about an invitation response
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  * @param invitationId Id of the invitation this response is for
  * @param wasAccepted Whether the invitation was accepted
  * @param wasBlocked Whether future invitations were blocked
  * @param creatorId Id of the user who accepted or rejected the invitation
  */
case class InvitationResponseData(invitationId: Int, wasAccepted: Boolean, wasBlocked: Boolean, creatorId: Int)
