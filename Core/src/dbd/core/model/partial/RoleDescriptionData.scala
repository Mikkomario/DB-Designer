package dbd.core.model.partial

import dbd.core.model.enumeration.UserRole
import dbd.core.model.template.DescriptionLinkLike
import utopia.flow.generic.ModelConvertible

/**
  * Contains basic information about a role description link
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class RoleDescriptionData[+D <: ModelConvertible](role: UserRole, description: D) extends DescriptionLinkLike[D]
{
	override def targetId = role.id
}
