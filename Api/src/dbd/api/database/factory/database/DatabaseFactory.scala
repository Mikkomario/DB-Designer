package dbd.api.database.factory.database

import dbd.api.database.Tables
import dbd.core.model.existing.database
import dbd.core.model.existing.database.{Database, DatabaseConfiguration}
import dbd.core.model.partial.database.DatabaseData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.{Deprecatable, LinkedFactory}

/**
 * Used for reading database (model) data from the database
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object DatabaseFactory extends LinkedFactory[Database, DatabaseConfiguration] with Deprecatable
{
	// IMPLEMENTED	--------------------------
	
	override def childFactory = DatabaseConfigurationFactory
	
	override def apply(model: Model[Constant], child: database.DatabaseConfiguration) =
		table.requirementDeclaration.validate(model).toTry.map { valid => database.Database(valid("id"),
			DatabaseData(valid("ownerId"), child, valid("creatorId"))) }
	
	override def nonDeprecatedCondition = childFactory.nonDeprecatedCondition
	
	override def table = Tables.database
}
