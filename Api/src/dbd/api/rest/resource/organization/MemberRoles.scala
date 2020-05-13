package dbd.api.rest.resource.organization

import dbd.api.database.access.many.UserRoles
import dbd.api.database.access.single
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.enumeration.TaskType.ChangeRoles
import dbd.core.model.enumeration.UserRole
import utopia.access.http.Method.{Delete, Post, Put}
import utopia.access.http.Status.{BadRequest, Forbidden, NotFound}
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * Used for interacting with organization member roles
  * @author Mikko Hilpinen
  * @since 11.5.2020, v2
  */
case class MemberRoles(organizationId: Int, userId: Option[Int]) extends Resource[AuthorizedContext]
{
	override val name = "roles"
	
	override val allowedMethods = Vector(Post, Put, Delete)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		// All methods require proper task-authorization
		context.authorizedForTask(organizationId, ChangeRoles) { (session, membershipId, connection) =>
			implicit val c: Connection = connection
			// Parses an array of role ids from the request body
			context.handleArrayPost { values =>
				val roles = values.flatMap { _.int }.flatMap { UserRole.forId(_).toOption }.toSet
				if (roles.isEmpty)
					Result.Failure(BadRequest, "Please specify one or more valid role ids in the request json body")
				else
				{
					// Checks whether self or another user was targeted
					userId.filterNot { _ == session.userId } match
					{
						case Some(targetUserId) =>
							single.User(targetUserId).membershipIdInOrganizationWithId(organizationId).pull match
							{
								case Some(targetMembershipId) =>
									// Can only modify the roles of a user that has a lower role
									// Also, can only add or delete those roles that the active user has themselves
									val activeUserRoles = single.Membership(membershipId).roles.toSet
									val illegalRoleModifications = roles -- activeUserRoles
									if (illegalRoleModifications.nonEmpty)
										Result.Failure(Forbidden, s"You cannot modify following role(s): [${
											illegalRoleModifications.toVector.map { _.id }.sorted.mkString(", ")}]")
									else
									{
										val targetUserRoles = single.Membership(targetMembershipId).roles.toSet
										if (activeUserRoles.forall(targetUserRoles.contains))
											Result.Failure(Forbidden, s"User $targetUserId has same or higher role as you do")
										else
										{
											val managedRoles = UserRoles.belowOrEqualTo(activeUserRoles)
											targetUserRoles.find { !managedRoles.contains(_) } match
											{
												case Some(conflictingRole) => Result.Failure(Forbidden,
													s"You don't have the right to adjust the roles of users with role ${
														conflictingRole.id}")
												case None =>
													// Performs the actual changes to the roles, according to method
													// and listed roles
													val method = context.request.method
													if (method == Post)
													{
														val newRoles = roles -- targetUserRoles
														if (newRoles.isEmpty)
															Result.Success(targetUserRoles.toVector.map { _.id }.sorted)
														else
														{
															// Adds new roles to the targeted user
															single.Membership(targetMembershipId).assignRoles(
																newRoles, session.userId)
															Result.Success((targetUserRoles ++ newRoles).map { _.id }
																.toVector.sorted)
														}
													}
													else if (method == Delete)
													{
														// The target user must be left with at least 1 role
														val rolesToRemove = targetUserRoles & roles
														if (rolesToRemove.size == targetUserRoles.size)
															Result.Failure(Forbidden,
																"The targeted user must be left with at least 1 role")
														else
														{
															// Removes the roles
															single.Membership(targetMembershipId).removeRoles(
																rolesToRemove)
															Result.Success((targetUserRoles -- rolesToRemove)
																.map { _.id }.toVector.sorted)
														}
													}
													else
													{
														if (targetUserRoles == roles)
															Result.Success(targetUserRoles.map { _.id }.toVector.sorted)
														else
														{
															// Adds & Removes roles to match the posted list
															val rolesToAssign = roles -- targetUserRoles
															val rolesToRemove = targetUserRoles -- roles
															if (rolesToRemove.nonEmpty)
																single.Membership(targetMembershipId)
																	.removeRoles(rolesToRemove)
															if (rolesToAssign.nonEmpty)
																single.Membership(targetMembershipId).assignRoles(
																	rolesToAssign, session.userId)
															Result.Success(roles.map { _.id }.toVector.sorted)
														}
													}
											}
										}
									}
								case None => Result.Failure(NotFound,
									s"The organization doesn't have a member with user id $targetUserId")
							}
						case None => Result.Failure(Forbidden, "You cannot edit your own roles")
					}
				}
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(message = Some(
		"Organization user roles doesn't have any sub-resources at this time"))
}