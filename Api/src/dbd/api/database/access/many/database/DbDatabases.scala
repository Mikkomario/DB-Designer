package dbd.api.database.access.many.database

import dbd.api.database.factory.database.DatabaseFactory
import dbd.api.database.model.database.DatabaseModel
import dbd.core.model.existing.database.Database
import dbd.core.model.partial.database.DatabaseData.NewDatabaseData
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyModelAccess

/**
  * An access point to all non-deprecated databases
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
object DbDatabases extends ManyModelAccess[Database]
{
	// IMPLEMENTED	------------------------
	
	override def factory = DatabaseFactory
	
	override def globalCondition = Some(factory.nonDeprecatedCondition)
	
	
	// OTHER	----------------------------
	
	/**
	  * Inserts a new database to the database
	  * @param newData First configuration for the new database
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted database
	  */
	def insert(newData: NewDatabaseData)(implicit connection: Connection) = DatabaseModel.insert(newData)
}
