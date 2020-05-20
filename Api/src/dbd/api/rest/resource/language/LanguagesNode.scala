package dbd.api.rest.resource.language

import dbd.api.database.access.many.{DbDescriptions, DbLanguages}
import dbd.api.database.access.single.DbUser
import dbd.api.rest.util.AuthorizedContext
import dbd.core.database.ConnectionPool
import dbd.core.model.combined.language.DescribedLanguage
import dbd.core.util.Log
import dbd.core.util.ThreadPool.executionContext
import utopia.access.http.Method.Get
import utopia.access.http.Status.InternalServerError
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

import scala.util.{Failure, Success}

/**
  * Used for accessing all specified languages
  * @author Mikko Hilpinen
  * @since 20.5.2020, v2
  */
object LanguagesNode extends Resource[AuthorizedContext]
{
	// IMPLEMENTED	--------------------------------
	
	override val name = "languages"
	
	override val allowedMethods = Vector(Get)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		// Checks which languages should be used when reading descriptions
		// Order = accept language > authorized user > all
		ConnectionPool.tryWith { implicit connection =>
			val accepted = context.requestedLanguages
			if (accepted.nonEmpty)
				get(accepted.map { _.id }).toResponse
			else if (context.request.headers.containsAuthorization)
			{
				context.sessionKeyAuthorized { (session, _) =>
					val userLanguages = DbUser(session.userId).languages.all.sortBy { _.familiarity.orderIndex }
						.map { _.languageId }
					get(userLanguages)
				}
			}
			else
				get(Vector()).toResponse
		} match
		{
			case Success(response) => response
			case Failure(error) =>
				Log(error, "Failed to read language descriptions")
				Result.Failure(InternalServerError, error.getMessage).toResponse
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(message = Some(
		"Languages doesn't currently contain any sub-nodes"))
	
	
	// OTHER	---------------------------------
	
	private def get(languageIds: Seq[Int])(implicit connection: Connection) =
	{
		// Reads all descriptions
		val descriptions =
		{
			if (languageIds.isEmpty)
				DbDescriptions.ofAllLanguages.all.groupBy { _.targetId }
			else
				DbDescriptions.ofAllLanguages.inLanguages(languageIds)
		}
		// Reads all languages
		val languages = DbLanguages.all
		// Combines languages with descriptions and returns them in response
		val combined = languages.map { language => DescribedLanguage(language,
			descriptions.getOrElse(language.id, Set()).toSet) }
		Result.Success(combined.map { _.toModel })
	}
}
