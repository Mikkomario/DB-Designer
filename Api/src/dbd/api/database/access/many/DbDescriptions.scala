package dbd.api.database.access.many

import dbd.api.database.model.description.{DescriptionLinkFactory, DeviceDescriptionModel, OrganizationDescriptionModel, RoleDescriptionModel, TaskDescriptionModel}
import dbd.core.model.enumeration.{DescriptionRole, TaskType, UserRole}
import dbd.core.model.existing
import dbd.core.model.existing.description.Description
import dbd.core.model.partial.description
import dbd.core.model.partial.description.DescriptionData
import dbd.core.model.post.NewDescription
import dbd.core.model.template.DescriptionLinkLike
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.access.ManyModelAccess
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
		DescriptionsOf(organizationId, OrganizationDescriptionModel)
	
	/**
	  * @param deviceId Device id
	  * @return An access point to that device's descriptions
	  */
	def ofDeviceWithId(deviceId: Int) =
		DescriptionsOf(deviceId, DeviceDescriptionModel)
	
	/**
	  * @param task Task type
	  * @return An access point to descriptions of that task type
	  */
	def ofTask(task: TaskType) = DescriptionsOf(task.id, TaskDescriptionModel)
	
	/**
	  * @param role User role
	  * @return An access point to descriptions of that user role
	  */
	def ofRole(role: UserRole) = DescriptionsOf(role.id, RoleDescriptionModel)
	
	
	// NESTED	----------------------------
	
	case class DescriptionsOf[+E, -P <: DescriptionLinkLike[DescriptionData]](targetId: Int,
																			  factory: DescriptionLinkFactory[E, Storable, P])
		extends ManyModelAccess[E]
	{
		// COMPUTED	------------------------
		
		private def descriptionFactory = factory.childFactory
		
		
		// IMPLEMENTED	--------------------
		
		override val globalCondition = Some(factory.withTargetId(targetId).toCondition && factory.nonDeprecatedCondition)
		
		
		// OTHER	------------------------
		
		/**
		  * @param languageId Id of targeted language
		  * @return An access point to a subset of these descriptions. Only contains desriptions written in that language.
		  */
		def inLanguageWithId(languageId: Int) = DescriptionsInLanguage(languageId)
		
		def update(newDescription: NewDescription, authorId: Int)(implicit connection: Connection): Map[Int, Description] =
		{
			// Updates each role + text pair separately
			newDescription.descriptions.map { case (role, text) =>
				update(description.DescriptionData(role, newDescription.languageId, text, Some(authorId)))
			}
		}
		
		def update(newDescription: DescriptionData)(implicit connection: Connection) =
		{
			// Must first deprecate the old version of this description
			deprecate(newDescription.languageId, newDescription.role)
			// Then inserts a new description
			factory.insert(targetId, newDescription)
		}
		
		def deprecate(languageId: Int, role: DescriptionRole)(implicit connection: Connection) =
		{
			// Needs to join into description table in order to filter by language id and role id
			// (factories automatically do this)
			val descriptionCondition = descriptionFactory.withRole(role).withLanguageId(languageId).toCondition
			factory.nowDeprecated.updateWhere(mergeCondition(descriptionCondition), Some(factory.target)) > 0
		}
		
		
		// NESTED	-------------------------
		
		case class DescriptionsInLanguage(languageId: Int) extends ManyModelAccess[E]
		{
			// IMPLEMENTED	-----------------
			
			override def factory = DescriptionsOf.this.factory
			
			override val globalCondition = Some(DescriptionsOf.this.mergeCondition(
				descriptionFactory.withLanguageId(languageId).toCondition))
			
			
			// OTHER	---------------------
			
			/**
			  * @param roles Targeted description roles
			  * @param connection DB Connection (implicit)
			  * @return Recorded descriptions for those roles (in this language & target)
			  */
			def apply(roles: Set[DescriptionRole])(implicit connection: Connection) =
			{
				if (roles.nonEmpty)
					read(Some(descriptionFactory.descriptionRoleIdColumn.in(roles.map { _.id })))
				else
					Vector()
			}
			
			def forRolesOutside(excludedRoles: Set[DescriptionRole])(implicit connection: Connection) =
				apply(DescriptionRole.values.toSet -- excludedRoles)
		}
	}
}
