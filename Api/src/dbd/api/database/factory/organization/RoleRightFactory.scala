package dbd.api.database.factory.organization

import dbd.api.database.Tables
import dbd.api.database.model.organization.RoleRightModel
import dbd.api.model.existing
import dbd.core.model.enumeration.{TaskType, UserRole}
import utopia.flow.datastructure.template.{Model, Property}
import utopia.vault.nosql.factory.FromRowModelFactory

object RoleRightFactory extends FromRowModelFactory[existing.RoleRight]
{
	// COMPUTED	------------------------------
	
	private def model = RoleRightModel
	
	
	// IMPLEMENTED	--------------------------
	
	/**
	  * @return Table used by this class/object
	  */
	def table = Tables.roleRight
	
	override def apply(model: Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		// Both enumeration values must be parseable
		UserRole.forId(valid(this.model.roleIdAttName).getInt).flatMap { role =>
			TaskType.forId(valid("taskId").getInt).map { task => existing.RoleRight(valid("id").getInt, role, task) }
		}
	}
}


