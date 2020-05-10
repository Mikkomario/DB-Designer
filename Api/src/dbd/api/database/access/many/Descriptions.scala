package dbd.api.database.access.many

import dbd.api.database.model.{DescriptionLinkFactory, DeviceDescription, OrganizationDescription, RoleDescription, TaskDescription}
import dbd.core.model.enumeration.{DescriptionRole, TaskType, UserRole}
import dbd.core.model.existing
import dbd.core.model.partial.DescriptionData
import dbd.core.model.post.NewDescription
import dbd.core.model.template.DescriptionLinkLike
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.access.ManyModelAccess

/**
  * Used for accessing various types of descriptions
  * @author Mikko Hilpinen
  * @since 10.5.2020, v2
  */
object Descriptions
{
	// OTHER	----------------------------
	
	/**
	  * @param organizationId Organization id
	  * @return An access point to that organization's descriptions
	  */
	def ofOrganizationWithId(organizationId: Int) =
		DescriptionsOf(organizationId, OrganizationDescription)
	
	/**
	  * @param deviceId Device id
	  * @return An access point to that device's descriptions
	  */
	def ofDeviceWithId(deviceId: Int) =
		DescriptionsOf(deviceId, DeviceDescription)
	
	/**
	  * @param task Task type
	  * @return An access point to descriptions of that task type
	  */
	def ofTask(task: TaskType) = DescriptionsOf(task.id, TaskDescription)
	
	/**
	  * @param role User role
	  * @return An access point to descriptions of that user role
	  */
	def ofRole(role: UserRole) = DescriptionsOf(role.id, RoleDescription)
	
	
	// NESTED	----------------------------
	
	case class DescriptionsOf[+E, -P <: DescriptionLinkLike[DescriptionData]](targetId: Int,
																			  factory: DescriptionLinkFactory[E, Storable, P])
		extends ManyModelAccess[E]
	{
		// IMPLEMENTED	--------------------
		
		override val globalCondition = Some(factory.withTargetId(targetId).toCondition && factory.nonDeprecatedCondition)
		
		
		// OTHER	------------------------
		
		def update(newDescription: NewDescription, authorId: Int)(implicit connection: Connection): Map[Int, existing.Description] =
		{
			// Updates each role + text pair separately
			newDescription.descriptions.map { case (role, text) =>
				update(DescriptionData(role, newDescription.languageId, text, Some(authorId)))
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
			val descriptionCondition = factory.childFactory.withRole(role).withLanguageId(languageId).toCondition
			val linkCondition = factory.withTargetId(targetId).toCondition
			factory.nowDeprecated.updateWhere(descriptionCondition && linkCondition && factory.nonDeprecatedCondition,
				Some(factory.target)) > 0
		}
	}
}
