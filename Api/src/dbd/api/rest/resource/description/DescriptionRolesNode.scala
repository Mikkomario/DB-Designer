package dbd.api.rest.resource.description

import dbd.api.database.access.many.description.DbDescriptions
import dbd.core.model.combined.description.DescribedDescriptionRole
import dbd.core.model.enumeration.DescriptionRole
import dbd.core.model.existing.description.DescriptionLink
import utopia.vault.database.Connection

/**
  * Used for retrieving descriptions of all description roles
  * @author Mikko Hilpinen
  * @since 20.5.2020, v2
  */
object DescriptionRolesNode extends PublicDescriptionsNode[DescriptionRole, DescribedDescriptionRole]
{
	// IMPLEMENTED	------------------------
	
	override val name = "description-roles"
	
	override protected def items(implicit connection: Connection) = DescriptionRole.values
	
	override protected def descriptionsAccess = DbDescriptions.ofAllDescriptionRoles
	
	override protected def idOf(item: DescriptionRole) = item.id
	
	override protected def combine(item: DescriptionRole, descriptions: Set[DescriptionLink]) =
		DescribedDescriptionRole(item, descriptions)
}
