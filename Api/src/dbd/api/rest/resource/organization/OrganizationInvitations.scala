package dbd.api.rest.resource.organization

import dbd.api.database.access.id.UserId
import dbd.api.database.access.single
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.enumeration.TaskType.InviteMembers
import dbd.core.model.existing.Invitation
import dbd.core.model.post.NewInvitation
import utopia.access.http.Method.Post
import utopia.access.http.Status.{BadRequest, Forbidden, NotImplemented, Unauthorized}
import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.TimeExtensions._
import utopia.flow.util.StringExtensions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

import scala.util.{Failure, Success}

/**
  * An access point to an organization's invitations
  * @author Mikko Hilpinen
  * @since 5.5.2020, v2
  */
case class OrganizationInvitations(organizationId: Int) extends Resource[AuthorizedContext]
{
	override val name = "invitations"
	
	override val allowedMethods = Vector(Post)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.sessionKeyAuthorized { (session, connection) =>
			implicit val c: Connection = connection
			// Makes sure the user belongs to the target organization
			single.User(session.userId).membershipIdInOrganizationWithId(organizationId).pull match
			{
				case Some(membershipId) =>
					// Makes sure the user has a right to make invitations in the first place
					if (single.Membership(membershipId).allows(InviteMembers))
					{
						// Parses the posted invitation
						context.handlePost(NewInvitation) { newInvitation =>
							newInvitation.validated match
							{
								case Success(validInvitation) =>
									// Makes sure the user has a right to give the specified role to another user
									if (single.Membership(membershipId).canPromoteTo(validInvitation.startingRole))
									{
										// Finds the user that is being invited (if registered)
										val recipientEmail = validInvitation.recipientEmail
										val recipientUserId = UserId.forEmail(recipientEmail)
										
										// Checks whether the user already is a member of this organization
										if (recipientUserId.exists { userId =>
											single.User(userId).isMemberInOrganizationWithId(organizationId) })
											Result.Success(invitationSendResultModel(wasInvitationSend = false,
												description = "The user was already a member of this organization"))
										else
										{
											// Makes sure the user hasn't blocked this organization from sending invites
											// And that there are no pending invitations for this user
											val accessInvitations = single.Organization(organizationId).invitations
											val blockedInvitations = accessInvitations.blocked
											val isBlocked = blockedInvitations.exists { i =>
												recipientUserId match
												{
													case Some(userId) => i.recipientId == userId
													case None => i.wrapped.recipientEmail.contains(recipientEmail)
												}
											}
											if (isBlocked)
												Result.Failure(Forbidden,
													"The recipient has blocked you from sending further invitations")
											else
											{
												accessInvitations.pending.find { i =>
													i.recipient match
													{
														case Right(userId) => recipientUserId.contains(userId)
														case Left(email) => email ~== recipientEmail
													}
												} match
												{
													case Some(pending) =>
														// If there was a pending invitation, won't send another but
														// registers this as a success
														Result.Success(invitationSendResultModel(
															wasInvitationSend = false, Some(pending),
															"There already existed a pending invitation for that user"))
													case None =>
														// Creates a new invitation and saves it
														val invitation = accessInvitations.send(
															recipientUserId.map { Right(_) }.getOrElse(Left(recipientEmail)),
															validInvitation.startingRole, session.userId,
															validInvitation.durationDays.days)
														Result.Success(invitationSendResultModel(wasInvitationSend = true,
															Some(invitation)))
												}
											}
										}
									}
									else
										Result.Failure(Forbidden, s"You're not allowed to promote a user to ${
											validInvitation.startingRole}")
								case Failure(error) => Result.Failure(BadRequest, error.getMessage)
							}
						}
					}
					else
						Result.Failure(Forbidden, s"You don't have the right to invite new members to this organization")
				case None => Result.Failure(Unauthorized, s"You're not a member of this organization")
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(NotImplemented,
		Some("Invitation access is not implemented yet"))
	
	
	// OTHER	---------------------------
	
	private def invitationSendResultModel(wasInvitationSend: Boolean, invitation: Option[Invitation] = None, description: String = "") =
	{
		Model(Vector("was_sent" -> wasInvitationSend, "invitation" -> invitation.map { _.toModel },
			"description" -> description.notEmpty))
	}
}