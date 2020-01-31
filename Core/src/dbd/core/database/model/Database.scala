package dbd.core.database.model

import dbd.core.database.Tables
import dbd.core.model.existing
import dbd.core.model.partial.NewDatabaseConfiguration
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.nosql.factory.{Deprecatable, LinkedStorableFactory}
import utopia.vault.sql.Insert

/**
 * Used for interacting with databases in DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object Database extends LinkedStorableFactory[existing.Database, existing.DatabaseConfiguration] with Deprecatable
{
	// IMPLEMENTED	--------------------------
	
	override def childFactory = DatabaseConfiguration
	
	override def apply(model: Model[Constant], child: existing.DatabaseConfiguration) =
		table.requirementDeclaration.validate(model).toTry.map { valid => existing.Database(valid("id").getInt, child) }
	
	override def nonDeprecatedCondition = DatabaseConfiguration.nonDeprecatedCondition
	
	override def table = Tables.database
	
	
	// OTHER	-------------------------------
	
	/**
	 * Inserts a new database to the DB
	 * @param data New database configuration
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted database
	 */
	def insert(data: NewDatabaseConfiguration)(implicit connection: Connection) =
	{
		// Inserts a new database row, then configuration for that database
		val databaseId = Insert(table, Model.empty).generatedIntKeys.head
		val newConfig = DatabaseConfiguration.insert(databaseId, data)
		existing.Database(databaseId, newConfig)
	}
}
