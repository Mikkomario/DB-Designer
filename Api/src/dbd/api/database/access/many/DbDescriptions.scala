package dbd.api.database.access.many

import dbd.api.database.factory.description.DescriptionLinkFactory
import dbd.api.database.model.description.{DescriptionLinkModel, DescriptionLinkModelFactory}
import dbd.core.model.enumeration.{DescriptionRole, TaskType, UserRole}
import dbd.core.model.existing.description.DescriptionLink
import dbd.core.model.partial.description
import dbd.core.model.partial.description.DescriptionData
import dbd.core.model.partial.description.DescriptionLinkData.PartialDescriptionLinkData
import dbd.core.model.post.NewDescription
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.sql.Condition
import utopia.vault.sql.Extensions._

/**
  * Used for accessing various types of descriptions
  * @author Mikko Hilpinen
  * @since 10.5.2020, v2
  */
object DbDescriptions
{
	// OTHER	----------------------------
	
	/**
	  * @param organizationId Organization id
	  * @return An access point to that organization's descriptions
	  */
	def ofOrganizationWithId(organizationId: Int) =
		DescriptionsOfSingle(organizationId, DescriptionLinkFactory.organization, DescriptionLinkModel.organization)
	
	/**
	  * @param organizationIds Organization ids
	  * @return An access point to descriptions of those organizations
	  */
	def ofOrganizationsWithIds(organizationIds: Set[Int]) = DescriptionsOfMany(organizationIds,
		DescriptionLinkFactory.organization, DescriptionLinkModel.organization)
	
	/**
	  * @param deviceId Device id
	  * @return An access point to that device's descriptions
	  */
	def ofDeviceWithId(deviceId: Int) =
		DescriptionsOfSingle(deviceId, DescriptionLinkFactory.device, DescriptionLinkModel.device)
	
	/**
	  * @param deviceIds Device ids
	  * @return An access point to descriptions of those devices
	  */
	def ofDevicesWithIds(deviceIds: Set[Int]) = DescriptionsOfMany(deviceIds,
		DescriptionLinkFactory.device, DescriptionLinkModel.device)
	
	/**
	  * @param task Task type
	  * @return An access point to descriptions of that task type
	  */
	def ofTask(task: TaskType) =
		DescriptionsOfSingle(task.id, DescriptionLinkFactory.task, DescriptionLinkModel.task)
	
	/**
	  * @param tasks Task types
	  * @return An access point to descriptions of those task types
	  */
	def ofTasks(tasks: Set[TaskType]) = DescriptionsOfMany(tasks.map { _.id },
		DescriptionLinkFactory.task, DescriptionLinkModel.task)
	
	/**
	  * @param role User role
	  * @return An access point to descriptions of that user role
	  */
	def ofRole(role: UserRole) =
		DescriptionsOfSingle(role.id, DescriptionLinkFactory.role, DescriptionLinkModel.role)
	
	/**
	  * @param roles Roles
	  * @return An access point to descriptions of those roles
	  */
	def ofRoles(roles: Set[UserRole]) = DescriptionsOfMany(roles.map { _.id },
		DescriptionLinkFactory.role, DescriptionLinkModel.role)
	
	/**
	  * @param languageId Language id
	  * @return An access point to that language's descriptions
	  */
	def ofLanguageWithId(languageId: Int) =
		DescriptionsOfSingle(languageId, DescriptionLinkFactory.language, DescriptionLinkModel.language)
	
	/**
	  * @param languageIds Language ids
	  * @return An access point to descriptions of languages with those ids
	  */
	def ofLanguagesWithIds(languageIds: Set[Int]) = DescriptionsOfMany(languageIds, DescriptionLinkFactory.language,
		DescriptionLinkModel.language)
	
	
	// NESTED	----------------------------
	
	case class DescriptionsOfMany(targetIds: Set[Int], factory: DescriptionLinkFactory[DescriptionLink],
								  modelFactory: DescriptionLinkModelFactory[Storable, PartialDescriptionLinkData])
		extends DescriptionLinksAccess[DescriptionLink]
	{
		// IMPLEMENTED	---------------------
		
		// Alters the condition based on the number of targeted items
		override val globalCondition =
		{
			if (targetIds.isEmpty)
				Some(Condition.alwaysFalse)
			else
			{
				val baseCondition =
				{
					if (targetIds.size == 1)
						modelFactory.withTargetId(targetIds.head).toCondition
					else
						modelFactory.targetIdColumn.in(targetIds)
				}
				Some(baseCondition && factory.nonDeprecatedCondition)
			}
		}
		
		
		// OTHER	-------------------------
		
		/**
		  * Reads descriptions for these items using the specified languages. Only up to one description per
		  * role per target is read.
		  * @param languageIds Ids of the targeted languages, from most preferred to least preferred (less preferred
		  *                    language ids are used when no results can be found with the more preferred options)
		  * @param connection DB Connection (implicit)
		  * @return Read descriptions, grouped by target id
		  */
		def inLanguages(languageIds: Seq[Int])(implicit connection: Connection): Map[Int, Vector[DescriptionLink]] =
		{
			languageIds.headOption match
			{
				case Some(languageId: Int) =>
					// In the first iteration, reads all descriptions. After that divides into sub-groups
					val readDescriptions = inLanguageWithId(languageId).all.groupBy { _.targetId }
					// Reads the rest of the data using recursion
					readRemaining(languageIds, DescriptionRole.values.toSet, this, readDescriptions)
				case None => Map()
			}
		}
		
		/**
		  * Reads descriptions for these items using the specified languages. Only up to one description per
		  * role per target is read.
		  * @param languageIds Ids of the targeted languages, from most preferred to least preferred (less preferred
		  *                    language ids are used when no results can be found with the more preferred options)
		  * @param roles Description roles that are read (will not target roles outside this set)
		  * @param connection DB Connection (implicit)
		  * @return Read descriptions, grouped by target id
		  */
		def inLanguages(languageIds: Seq[Int], roles: Set[DescriptionRole])
					   (implicit connection: Connection): Map[Int, Vector[DescriptionLink]] =
		{
			if (languageIds.isEmpty || targetIds.isEmpty || roles.isEmpty)
				Map()
			else
				inLanguages(targetIds, languageIds, roles)
		}
		
		// Language ids must not be empty (neither should any other parameter)
		private def inLanguages(remainingTargetIds: Set[Int], languageIds: Seq[Int], remainingRoles: Set[DescriptionRole])(
			implicit connection: Connection): Map[Int, Vector[DescriptionLink]] =
		{
			// Reads descriptions in target languages until either all description types have been read or all language
			// options exhausted
			val languageId = languageIds.head
			val newAccessPoint = subGroup(remainingTargetIds)
			// Target id -> Descriptions
			val readDescriptions = newAccessPoint.inLanguageWithId(languageId)(remainingRoles).groupBy { _.targetId }
			
			// Reads the rest of the descriptions recursively
			readRemaining(languageIds, remainingRoles, newAccessPoint, readDescriptions)
		}
		
		// Continues read through recursion, if possible. Utilizes (and includes) existing read results.
		// LanguageIds and roles should be passed as they were at the start of the last read
		private def readRemaining(languageIds: Seq[Int],
								  remainingRoles: Set[DescriptionRole], lastAccessPoint: DescriptionsOfMany,
								  lastReadResults: Map[Int, Vector[DescriptionLink]])
								 (implicit connection: Connection): Map[Int, Vector[DescriptionLink]] =
		{
			val remainingLanguageIds = languageIds.tail
			if (remainingLanguageIds.nonEmpty)
			{
				// Groups the remaining items based on the remaining roles without a description
				// Remaining roles -> target ids
				val remainingRolesWithTargets = lastReadResults.groupMap { case (_, descriptions) =>
					remainingRoles -- descriptions.map { _.description.role } } { case (targetId, _) => targetId }
					.filter { _._1.nonEmpty }
				
				// Performs additional queries for each remaining role group (provided there are languages left)
				if (remainingRolesWithTargets.isEmpty)
					lastReadResults
				else
				{
					val recursiveReadResults = remainingRolesWithTargets.map { case (roles, targetIds) =>
						lastAccessPoint.inLanguages(targetIds.toSet, remainingLanguageIds, roles) }
						.reduce { _ ++ _ }
					lastReadResults ++ recursiveReadResults
				}
			}
			else
				lastReadResults
		}
		
		// Accesses a sub-group of these descriptions
		private def subGroup(remainingTargetIds: Set[Int]) =
		{
			if (remainingTargetIds == targetIds)
				this
			else
				copy(targetIds = remainingTargetIds)
		}
	}
	
	case class DescriptionsOfSingle(targetId: Int, factory: DescriptionLinkFactory[DescriptionLink],
									modelFactory: DescriptionLinkModelFactory[Storable, PartialDescriptionLinkData])
		extends DescriptionLinksAccess[DescriptionLink]
	{
		// IMPLEMENTED	--------------------
		
		override val globalCondition = Some(modelFactory.withTargetId(targetId).toCondition && factory.nonDeprecatedCondition)
		
		
		// OTHER	-------------------------
		
		/**
		  * Updates possibly multiple descriptions for this item (will replace old description versions)
		  * @param newDescription New description to insert
		  * @param authorId Id of the user who wrote this description
		  * @param connection DB Connection (implicit)
		  * @return Newly inserted description links
		  */
		def update(newDescription: NewDescription, authorId: Int)(implicit connection: Connection): Vector[DescriptionLink] =
		{
			// Updates each role + text pair separately
			newDescription.descriptions.map { case (role, text) =>
				update(description.DescriptionData(role, newDescription.languageId, text, Some(authorId)))
			}.toVector
		}
		
		/**
		  * Updates a single description for this item
		  * @param newDescription New description
		  * @param connection DB Connection
		  * @return Newly inserted description
		  */
		def update(newDescription: DescriptionData)(implicit connection: Connection) =
		{
			// Must first deprecate the old version of this description
			deprecate(newDescription.languageId, newDescription.role)
			// Then inserts a new description
			modelFactory.insert(targetId, newDescription)
		}
		
		/**
		  * Updates a single description for this item
		  * @param newDescriptionRole Role of the new description
		  * @param languageId Id of the language the new description is written in
		  * @param authorId Id of the user who wrote this description
		  * @param text Description text
		  * @param connection DB Connection (implicit)
		  * @return Newly inserted description
		  */
		def update(newDescriptionRole: DescriptionRole, languageId: Int, authorId: Int, text: String)
				  (implicit connection: Connection): DescriptionLink = update(DescriptionData(newDescriptionRole,
			languageId, text, Some(authorId)))
		
		/**
		  * Deprecates a description for this item
		  * @param languageId Id of the language the description is written in
		  * @param role Targeted description role
		  * @param connection DB Connection (implicit)
		  * @return Whether a description was deprecated
		  */
		def deprecate(languageId: Int, role: DescriptionRole)(implicit connection: Connection) =
		{
			// Needs to join into description table in order to filter by language id and role id
			// (factories automatically do this)
			val descriptionCondition = descriptionFactory.withRole(role).withLanguageId(languageId).toCondition
			modelFactory.nowDeprecated.updateWhere(mergeCondition(descriptionCondition), Some(factory.target)) > 0
		}
		
		/**
		  * @param languageIds Ids of the targeted languages (in order from most to least preferred)
		  * @param connection DB Connection (implicit)
		  * @return This item's descriptions in specified languages (secondary languages are used when no primary
		  *         language description is found)
		  */
		def inLanguages(languageIds: Seq[Int])(implicit connection: Connection): Vector[DescriptionLink] =
		{
			languageIds.headOption match
			{
				case Some(languageId) =>
					val readDescriptions = inLanguageWithId(languageId).all
					val missingRoles = DescriptionRole.values.toSet -- readDescriptions.map { _.description.role }.toSet
					if (missingRoles.nonEmpty)
						readDescriptions ++ inLanguages(languageIds.tail, missingRoles)
					else
						readDescriptions
				case None => Vector()
			}
		}
		
		/**
		  * @param languageIds Ids of the targeted languages (in order from most to least preferred)
		  * @param remainingRoles Roles that need descriptions
		  * @param connection DB Connection (implicit)
		  * @return This item's descriptions in specified languages (secondary languages are used when no primary
		  *         language description is found)
		  */
		def inLanguages(languageIds: Seq[Int], remainingRoles: Set[DescriptionRole])(
			implicit connection: Connection): Vector[DescriptionLink] =
		{
			// Reads descriptions in target languages until either all description types have been read or all language
			// options exhausted
			languageIds.headOption match
			{
				case Some(languageId) =>
					val readDescriptions = inLanguageWithId(languageId)(remainingRoles)
					val newRemainingRoles = remainingRoles -- readDescriptions.map { _.description.role }
					if (remainingRoles.nonEmpty)
						readDescriptions ++ inLanguages(languageIds.tail, newRemainingRoles)
					else
						readDescriptions
				case None => Vector()
			}
		}
	}
}
