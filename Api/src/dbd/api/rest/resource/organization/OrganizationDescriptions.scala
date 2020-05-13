package dbd.api.rest.resource.organization

import dbd.api.database.access.many.Languages
import dbd.api.database.access.single
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.enumeration.DescriptionRole
import dbd.core.model.enumeration.TaskType.DocumentOrganization
import dbd.core.model.existing.OrganizationDescription
import dbd.core.model.partial.OrganizationDescriptionData
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
case class OrganizationDescriptions(organizationId: Int) extends Resource[AuthorizedContext]
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
					val descriptions = inLanguages(languages.map { _.id })
					Result.Success(descriptions.map { _.toModel })
				}
				else
				{
					val availableLanguages = Languages.all
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
					if (single.Language(newDescription.languageId).isDefined)
					{
						// Updates the organization's descriptions accordingly
						val insertedDescriptions = single.Organization(organizationId).descriptions.update(newDescription,
							session.userId).map { case (linkId, description) => OrganizationDescription(
							linkId, OrganizationDescriptionData(organizationId, description)) }.toVector
						// Returns new version of organization's descriptions (in specified language)
						val otherDescriptions =
						{
							val missingRoles = DescriptionRole.values.toSet -- insertedDescriptions.map { _.description.role }.toSet
							if (missingRoles.nonEmpty)
								inLanguages(Vector(newDescription.languageId), missingRoles)
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
	
	
	// OTHER	---------------------------------
	
	private def inLanguages(languageIds: Seq[Int])(implicit connection: Connection): Vector[OrganizationDescription] =
	{
		languageIds.headOption match
		{
			case Some(languageId) =>
				val readDescriptions = single.Organization(organizationId).descriptions.inLanguageWithId(languageId).all
				val missingRoles = DescriptionRole.values.toSet -- readDescriptions.map { _.description.role }.toSet
				if (missingRoles.nonEmpty)
					readDescriptions ++ inLanguages(languageIds.tail, missingRoles)
				else
					readDescriptions
			case None => Vector()
		}
	}
	
	private def inLanguages(languageIds: Seq[Int], remainingRoles: Set[DescriptionRole])(
		implicit connection: Connection): Vector[OrganizationDescription] =
	{
		// Reads descriptions in target languages until either all description types have been read or all language
		// options exhausted
		languageIds.headOption match
		{
			case Some(languageId) =>
				val readDescriptions = single.Organization(organizationId).descriptions.inLanguageWithId(
					languageId)(remainingRoles)
				val newRemainingRoles = remainingRoles -- readDescriptions.map { _.description.role }
				if (remainingRoles.nonEmpty)
					readDescriptions ++ inLanguages(languageIds.tail, newRemainingRoles)
				else
					readDescriptions
			case None => Vector()
		}
	}
}