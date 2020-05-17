package dbd.api.rest.resource.user

import dbd.api.database.access.single.DbUser
import dbd.api.rest.util.AuthorizedContext
import utopia.access.http.Method.{Delete, Get, Post, Put}
import utopia.access.http.Status.Forbidden
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

/**
  * Used for interacting with the languages known to the authorized user
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
object MyLanguages extends Resource[AuthorizedContext]
{
	// IMPLEMENTED	-------------------------
	
	override val name = "languages"
	
	override val allowedMethods = Vector(Get, Post, Put, Delete)
	
	override def toResponse(remainingPath: Option[Path])(implicit context: AuthorizedContext) =
	{
		context.sessionKeyAuthorized { (session, connection) =>
			implicit val c: Connection = connection
			val method = context.request.method
			val user = DbUser(session.userId)
			val existingLanguageIds = user.languageIds.toSet
			
			if (method == Get)
			{
				// Reads language descriptions from the database, uses languages in the accept-language header
				???
			}
			else
			{
				context.handleArrayPost { values =>
					val postedLanguageIds = values.flatMap { _.int }.toSet
					// on POST, adds new language ids
					if (method == Post)
					{
						val newLanguageIds = postedLanguageIds -- existingLanguageIds
						if (newLanguageIds.nonEmpty)
							user.addLanguagesWithIds(newLanguageIds)
						Result.Success((existingLanguageIds ++ newLanguageIds).toVector)
					}
					// on DELETE, removes language ids
					else if (method == Delete)
					{
						// Makes sure at leas one language remains
						if (existingLanguageIds.forall(postedLanguageIds.contains))
							Result.Failure(Forbidden, "You must keep at least one language")
						else
						{
							user.removeLanguagesWithIds(postedLanguageIds)
							Result.Success((existingLanguageIds -- postedLanguageIds).toVector)
						}
					}
					// on PUT, replaces language ids
					else
					{
						if (postedLanguageIds.isEmpty)
							Result.Failure(Forbidden, "You must keep at least one language")
						else
						{
							val newLanguageIds = postedLanguageIds -- existingLanguageIds
							val removedLanguageIds = existingLanguageIds -- postedLanguageIds
							user.removeLanguagesWithIds(removedLanguageIds)
							user.addLanguagesWithIds(newLanguageIds)
							Result.Success(postedLanguageIds.toVector)
						}
					}
				}
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(
		message = Some("languages doesn't have any sub-resources at this time"))
}
