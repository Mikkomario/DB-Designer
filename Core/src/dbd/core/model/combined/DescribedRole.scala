package dbd.core.model.combined

import dbd.core.model.enumeration.UserRole
import dbd.core.model.existing.RoleDescription
import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Adds descriptive data to a user role
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  * @param role Wrapped role
  * @param descriptions Various descriptions for this role
  * @param tasks Tasks allowed for users with this role, along with their descriptions
  */
case class DescribedRole(role: UserRole, descriptions: Set[RoleDescription], tasks: Set[DescribedTask])
	extends ModelConvertible
{
	override def toModel = Model(Vector("id" -> role.id,
		"descriptions" -> descriptions.map { _.toModel }.toVector,
		"allowed_tasks" -> tasks.map { _.toModel }.toVector))
}
