package dbd.api.rest.resource.user

import dbd.api.database.access.single
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.existing.InvitationWithResponse
import dbd.core.model.post.NewInvitationResponse
import utopia.access.http.Method.Post
import utopia.access.http.Status.{Forbidden, NotFound, Unauthorized}
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * A rest resource that allows access to an invitation response
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class InvitationResponse(invitationId: Int) extends Resource[AuthorizedContext]
{
	// IMPLEMENTED	--------------------------
	
	override val name = "response"
	
	override val allowedMethods = Vector(Post)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.sessionKeyAuthorized { (session, connection) =>
			// Parses the response
			context.handlePost(NewInvitationResponse) { newResponse =>
				// Makes sure the invitation exists, hasn't been answered or expired yet and is targeted for this user
				implicit val c: Connection = connection
				val accessInvitation = single.Invitation(invitationId)
				accessInvitation.pull match
				{
					case Some(invitation) =>
						val isForThisUser = invitation.recipient match
						{
							case Right(recipientId) => recipientId == session.userId
							case Left(recipientEmail) =>
								val myEmail = single.User(session.userId).settings.map { _.email }
								myEmail.contains(recipientEmail)
						}
						if (isForThisUser)
						{
							if (invitation.hasExpired)
								Result.Failure(Forbidden, "This invitation has already expired")
							else
							{
								accessInvitation.response.pull match
								{
									case Some(earlierResponse) =>
										// If there was a response, will not create a new one
										if (earlierResponse.wasAccepted == newResponse.wasAccepted &&
											earlierResponse.wasBlocked == newResponse.wasBlocked)
											Result.Success(InvitationWithResponse(invitation, earlierResponse).toModel)
										else
											Result.Failure(Forbidden, "You've already responded to this invitation")
									case None =>
										// Saves the new response to DB
										val savedResponse = accessInvitation.response.insert(newResponse, session.userId)
										Result.Success(InvitationWithResponse(invitation, savedResponse).toModel)
								}
							}
						}
						else
							Result.Failure(Unauthorized, "This invitation is not for you")
					case None => Result.Failure(NotFound, s"There doesn't exist an invitation with id $invitationId")
				}
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(message = Some(
		"Invitation response doesn't have any child nodes"))
}
