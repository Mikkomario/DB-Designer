package dbd.core.database

import dbd.core.model.existing
import dbd.core.model.existing.database.{Attribute, AttributeConfiguration}
import utopia.vault.nosql.access.ManyModelAccess

/**
  * Used for accessing multiple attributes at a time
  * @author Mikko Hilpinen
  * @since 19.2.2020, v0.1
  */
object Attributes extends ManyModelAccess[Attribute]
{
	// IMPLEMENTED	-----------------------------
	
	override def factory = model.Attribute
	
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
		
		override def configurationFactory = model.AttributeConfiguration
		
		override def factory = Attributes.this.factory
		
		// Requires join with class for global search condition
		override def target = super.target join model.Class.table
		
		// Uses class.databaseId in search condition
		override def globalCondition = Some(Attributes.this.mergeCondition(
			model.Class.withDatabaseId(databaseId).toCondition))
	}
}
