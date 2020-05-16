package dbd.api.database.model.description

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.enumeration.UserRole
import dbd.core.model.existing.description
import dbd.core.model.existing.description.Description
import dbd.core.model.{existing, partial}
import dbd.core.model.partial.description.{DescriptionData, RoleDescriptionData}

object RoleDescriptionModel extends DescriptionLinkFactory[description.RoleDescription, RoleDescriptionModel,
	RoleDescriptionData[DescriptionData]]
{
	override def targetIdAttName = "roleId"
	
	override protected def apply(id: Int, targetId: Int, description: Description) =
	{
		UserRole.forId(targetId).map { role =>
			existing.description.RoleDescription(id, partial.description.RoleDescriptionData(role, description))
		}
	}
	
	override def table = Tables.roleDescription
}

/**
  * Used for interacting with role-description links in DB
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class RoleDescriptionModel(id: Option[Int] = None, roleId: Option[Int] = None, descriptionId: Option[Int] = None,
								deprecatedAfter: Option[Instant] = None)
	extends DescriptionLinkModel[description.RoleDescription, RoleDescriptionModel.type]
{
	override def factory = RoleDescriptionModel
	
	override def targetId = roleId
}
