package dbd.api.database.model.description

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.enumeration.UserRole
import dbd.core.model.existing
import dbd.core.model.partial.{DescriptionData, RoleDescriptionData}

object RoleDescription extends DescriptionLinkFactory[existing.RoleDescription, RoleDescription,
	RoleDescriptionData[DescriptionData]]
{
	override def targetIdAttName = "roleId"
	
	override protected def apply(id: Int, targetId: Int, description: existing.Description) =
	{
		UserRole.forId(targetId).map { role =>
			existing.RoleDescription(id, RoleDescriptionData(role, description))
		}
	}
	
	override def table = Tables.roleDescription
}

/**
  * Used for interacting with role-description links in DB
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class RoleDescription(id: Option[Int] = None, roleId: Option[Int] = None, descriptionId: Option[Int] = None,
						   deprecatedAfter: Option[Instant] = None)
	extends DescriptionLink[existing.RoleDescription, RoleDescription.type]
{
	override def factory = RoleDescription
	
	override def targetId = roleId
}
