package dbd.api.rest.resource.organization

import dbd.api.database.access.many.{DbDescriptions, DbLanguages}
import dbd.api.database.access.single
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.enumeration.DescriptionRole
import dbd.core.model.enumeration.TaskType.DocumentOrganization
import dbd.core.model.post.NewDescription
import utopia.access.http.Method.{Get, Put}
import utopia.access.http.Status.{BadRequest, NotFound}
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * A rest-resource used for accessing and updating organization descriptions
  * @author Mikko Hilpinen
  * @since 10.5.2020, v2
  */
case class OrganizationDescriptionsNode(organizationId: Int) extends Resource[AuthorizedContext]
{
	// IMPLEMENTED	-----------------------------
	
	override val name = "descriptions"
	
	override val allowedMethods = Vector(Get, Put)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		// In GET request, reads descriptions in requested languages
		if (context.request.method == Get)
		{
			context.authorizedInOrganization(organizationId) { (_, _, connection) =>
				implicit val c: Connection = connection
				// Checks the languages the user wants to use and gathers descriptions in those languages
				val languages = context.requestedLanguages
				if (languages.nonEmpty)
				{
					val descriptions = DbDescriptions.ofOrganizationWithId(organizationId).inLanguages(languages.map { _.id })
					Result.Success(descriptions.map { _.toModel })
				}
				else
				{
					val availableLanguages = DbLanguages.all
					Result.Failure(BadRequest,
						s"Please specify an Accept-Language header with one or more of following options: [${
							availableLanguages.map { _.isoCode }.mkString(", ")}]")
				}
			}
		}
		// In PUT request, updates descriptions based on posted model
		else
		{
			// Authorizes the request and parses posted description(s)
			context.authorizedForTask(organizationId, DocumentOrganization) { (session, _, connection) =>
				context.handlePost(NewDescription) { newDescription =>
					implicit val c: Connection = connection
					// Makes sure language id is valid
					if (single.DbLanguage(newDescription.languageId).isDefined)
					{
						// Updates the organization's descriptions accordingly
						val dbDescriptions = DbDescriptions.ofOrganizationWithId(organizationId)
						val insertedDescriptions = dbDescriptions.update(newDescription, session.userId)
						// Returns new version of organization's descriptions (in specified language)
						val otherDescriptions =
						{
							val missingRoles = DescriptionRole.values.toSet -- insertedDescriptions.map { _.description.role }.toSet
							if (missingRoles.nonEmpty)
								dbDescriptions.inLanguages(Vector(newDescription.languageId), missingRoles)
							else
								Vector()
						}
						Result.Success((insertedDescriptions ++ otherDescriptions).map { _.toModel })
					}
					else
						Result.Failure(NotFound, s"There doesn't exist any language with id ${newDescription.languageId}")
				}
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(message =
		Some("Organization descriptions doesn't have any sub-resources at this time"))
}