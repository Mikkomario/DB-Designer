package dbd.api.database.access.many.database

import dbd.api.database.access.many.description.ChangedModelsAccess
import dbd.api.database.model.database
import dbd.api.database.model.database.ClassModel
import dbd.core.model.existing.database.{Attribute, AttributeConfiguration}
import utopia.vault.nosql.access.ManyModelAccess

/**
  * Used for accessing multiple attributes at a time
  * @author Mikko Hilpinen
  * @since 19.2.2020, v0.1
  */
object DbAttributes extends ManyModelAccess[Attribute]
{
	// IMPLEMENTED	-----------------------------
	
	override def factory = database.AttributeModel
	
	override def globalCondition = Some(factory.nonDeprecatedCondition)
	
	
	// COMPUTED	--------------------------------
	
	/**
	  * @param databaseId Id of targeted database
	  * @return An access point to attributes in that database
	  */
	def inDatabaseWithId(databaseId: Int) = new AttributesInDatabase(databaseId)
	
	
	// NESTED	----------------------------------
	
	class AttributesInDatabase(databaseId: Int) extends ChangedModelsAccess[Attribute, AttributeConfiguration]
	{
		override def creationTimeColumn = factory.creationTimeColumn
		
		override def configurationFactory = database.AttributeConfigurationModel
		
		override def factory = DbAttributes.this.factory
		
		// Requires join with class for global search condition
		override def target = super.target join ClassModel.table
		
		// Uses class.databaseId in search condition
		override def globalCondition = Some(DbAttributes.this.mergeCondition(
			ClassModel.withDatabaseId(databaseId).toCondition))
	}
}