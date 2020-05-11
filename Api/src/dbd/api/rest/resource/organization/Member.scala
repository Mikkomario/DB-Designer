package dbd.api.rest.resource.organization

import dbd.api.database.access.single
import dbd.api.database.access.many.{TaskTypes, UserRoles}
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.enumeration.TaskType.RemoveMember
import utopia.access.http.Method.Delete
import utopia.access.http.Status.Forbidden
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * A rest-resource for targeting organization members
  * @author Mikko Hilpinen
  * @since 11.5.2020, v2
  */
case class Member(organizationId: Int, userId: Int) extends Resource[AuthorizedContext]
{
	override def name = userId.toString
	
	override val allowedMethods = Vector(Delete)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		// The user needs to be authorized for the task
		context.authorizedForTask(organizationId, RemoveMember) { (_, membershipId, connection) =>
			implicit val c: Connection = connection
			// Finds targeted membership id
			single.User(userId).membershipIdInOrganizationWithId(organizationId).pull match
			{
				case Some(targetMembershipId) =>
					// Checks the roles of the active and targeted user. Targeted user can only be removed if they
					// have a role lower than the active user's
					val activeUserRoles = single.Membership(membershipId).roles.toSet
					val targetUserRoles = single.Membership(targetMembershipId).roles.toSet
					if (activeUserRoles.forall(targetUserRoles.contains))
						Result.Failure(Forbidden, s"User $userId has same or higher role as you do")
					else
					{
						val managedRoles = UserRoles.allowingOnly(TaskTypes.forRoleCombination(activeUserRoles).toSet)
						targetUserRoles.find { !managedRoles.contains(_) } match
						{
							case Some(conflictingRole) => Result.Failure(Forbidden,
								s"You don't have the right to remove members of role ${conflictingRole.id}")
							case None =>
								// If rights are OK, ends the targeted membership
								single.Membership(targetMembershipId).end()
								Result.Empty
						}
					}
				case None => Result.Empty
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(message = Some(
		"Organization member doesn't have any sub-resources at this time"))
}
