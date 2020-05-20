package dbd.core.model.combined.organization

import dbd.core.model.combined.description.Described
import dbd.core.model.existing.description.DescriptionLink

/**
  * Adds descriptive data to a user role
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  * @param role Wrapped role with associated rights
  * @param descriptions Various descriptions for this role
  */
case class DescribedRole(role: RoleWithRights, descriptions: Set[DescriptionLink]) extends Described[RoleWithRights]
{
	override def wrapped = role
}
