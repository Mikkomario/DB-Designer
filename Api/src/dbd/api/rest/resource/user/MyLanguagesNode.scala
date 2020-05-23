package dbd.api.rest.resource.user

import dbd.api.database.access.single.DbLanguage
import dbd.api.database.access.single.user.DbUser
import dbd.api.rest.util.AuthorizedContext
import dbd.core.model.post.NewLanguageProficiency
import utopia.access.http.Method.{Delete, Get, Post, Put}
import utopia.access.http.Status.{BadRequest, Forbidden}
import utopia.flow.generic.ValueConversions._
import utopia.nexus.http.Path
import utopia.nexus.rest.Resource
import utopia.nexus.rest.ResourceSearchResult.Error
import utopia.nexus.result.Result
import utopia.vault.database.Connection

import scala.util.{Failure, Success}

/**
  * Used for interacting with the languages known to the authorized user
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
object MyLanguagesNode extends Resource[AuthorizedContext]
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
			
			// GET simply returns existing user language links (with appropriate descriptions)
			if (method == Get)
			{
				// Reads language descriptions from the database, uses languages in the accept-language header
				val languages = user.languages.withDescriptionsInLanguages(context.languageIdListFor(session.userId))
					.sortBy { _.familiarity.orderIndex }
				Result.Success(languages.map { _.toModelWithoutUser })
			}
			// DELETE removes known languages
			else if (method == Delete)
			{
				context.handleArrayPost { values =>
					val existingLanguageIds = user.languageIds.toSet
					val languageIdsToDelete = values.flatMap { _.int }.toSet
					// Makes sure at leas one language remains
					if (existingLanguageIds.forall(languageIdsToDelete.contains))
						Result.Failure(Forbidden, "You must keep at least one language")
					else
					{
						user.languages.remove(languageIdsToDelete)
						// Returns a list of remaining language ids
						Result.Success((existingLanguageIds -- languageIdsToDelete).toVector)
					}
				}
			}
			else
			{
				context.handleModelArrayPost(NewLanguageProficiency) { proficiencies =>
					// Validates the proposed languages first
					DbLanguage.validateProposedProficiencies(proficiencies) match
					{
						case Success(proficiencies) =>
							// on POST, adds new language proficiencies (may overwrite existing)
							// on PUT, replaces languages
							if (proficiencies.isEmpty && method == Put)
								Result.Failure(Forbidden, "You must keep at least one language")
							else
							{
								val existingLanguages = user.languages.all
								
								// Groups the changes
								val existingLanguagesMap = existingLanguages.map { l => l.languageId -> l.familiarity }.toMap
								val changesMap = proficiencies.toMap
								
								val changesInExisting = existingLanguagesMap.flatMap { case (languageId, familiarity) =>
									changesMap.get(languageId).map { newFamiliarity =>
										languageId -> (familiarity -> newFamiliarity) } }
								val modifications = changesInExisting.filterNot { case (_, change) => change._1 == change._2 }
								val duplicateLanguageIds = changesInExisting.filter { case (_, change) =>
									change._1 == change._2 }.keySet
								val newLanguages = changesMap.filterNot { case (languageId, _) =>
									existingLanguagesMap.contains(languageId) }
								
								// Removes some languages (those not listed in PUT and those being modified)
								val languageIdsToRemove =
								{
									val base = modifications.keySet
									if (method == Put)
										base ++ existingLanguagesMap.filterNot { case (languageId, _) =>
											changesMap.contains(languageId) }.keySet
									else
										base
								}
								user.languages.remove(languageIdsToRemove)
								
								// Adds new language links
								val inserted = (modifications.map { case (languageId, change) =>
									languageId -> change._2 } ++ newLanguages).map { case (languageId, familiarity) =>
									user.languages.insert(languageId, familiarity) }
								
								// Returns the new languages list
								val duplicates = existingLanguages.filter { l => duplicateLanguageIds.contains(l.languageId) }
								if (method == Put)
									Result.Success((duplicates ++ inserted).map { _.toModelWithoutUser })
								else
									Result.Success((existingLanguages.filterNot { l => changesMap.contains(l.languageId) } ++
										duplicates ++ inserted).map { _.toModelWithoutUser })
							}
						case Failure(error) => Result.Failure(BadRequest, error.getMessage)
					}
				}
			}
		}
	}
	
	override def follow(path: Path)(implicit context: AuthorizedContext) = Error(
		message = Some("languages doesn't have any sub-resources at this time"))
}