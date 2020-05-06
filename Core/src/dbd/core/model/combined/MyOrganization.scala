package dbd.core.model.combined

import dbd.core.model.existing.OrganizationDescription
import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Contains more information about an organization from a single member's perspective
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  * @param id Organization id
  * @param userId Id of the described user
  * @param descriptions Various descriptions of this organization
  * @param myRoles Described user's roles in this organization
  */
case class MyOrganization(id: Int, userId: Int, descriptions: Set[OrganizationDescription], myRoles: Set[DescribedRole])
	extends ModelConvertible
{
	override def toModel =
	{
		val organizationModel = Model(Vector("id" -> id, "descriptions" -> descriptions.map { _.toModel }.toVector))
		val userModel = Model(Vector("id" -> userId, "roles" -> myRoles.map { _.toModel }.toVector))
		Model(Vector("organization" -> organizationModel, "user" -> userModel))
	}
}
