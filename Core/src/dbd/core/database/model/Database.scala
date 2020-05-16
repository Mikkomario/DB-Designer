package dbd.core.database.model

import dbd.core.database.Tables
import dbd.core.model.existing.database
import dbd.core.model.partial.database.NewDatabaseConfiguration
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.nosql.factory.{Deprecatable, LinkedFactory}
import utopia.vault.sql.Insert

/**
 * Used for interacting with databases in DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object Database extends LinkedFactory[database.Database, database.DatabaseConfiguration] with Deprecatable
{
	// IMPLEMENTED	--------------------------
	
	override def childFactory = DatabaseConfiguration
	
	override def apply(model: Model[Constant], child: database.DatabaseConfiguration) =
		table.requirementDeclaration.validate(model).toTry.map { valid => database.Database(valid("id").getInt, child) }
	
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
		database.Database(databaseId, newConfig)
	}
}
