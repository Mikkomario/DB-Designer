package dbd.core.model.partial

import java.time.Instant

import dbd.core.model.enumeration.UserRole

/**
  * Contains basic information about an invitation to join an organization
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  * @param organizationId Id of the organization the user is invited to
  * @param recipient Either Right: Recipient user id or Left: Recipient user email address
  * @param startingRole Role assigned to the user in the organization, initially
  * @param expireTime Timestamp when the invitation will expire
  * @param creatorId Id of the user who created this invitation (optional)
  */
case class InvitationData(organizationId: Int, recipient: Either[String, Int], startingRole: UserRole,
						  expireTime: Instant, creatorId: Option[Int] = None)
