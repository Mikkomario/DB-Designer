package dbd.core.database

import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import dbd.core.model.existing.{Attribute, AttributeConfiguration}
import dbd.core.model.partial.{NewAttribute, NewAttributeConfiguration}
import utopia.vault.database.Connection
import utopia.vault.model.immutable.access.{ConditionalManyAccess, ConditionalSingleAccess, ItemAccess, NonDeprecatedSingleAccess}
import utopia.vault.sql.Where

/**
 * Used for accessing individual classes from DB
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
object Class extends NonDeprecatedSingleAccess[existing.Class]
{
	// IMPLEMENTED	-------------------
	
	override def factory = model.Class
	
	
	// OTHER	-----------------------
	
	/**
	 * @param id Class id
	 * @return An access point to that specific class
	 */
	def apply(id: Int) = new ClassById(id)
	
	
	// NESTED	-----------------------
	
	/**
	 * Used for accessing individual class' data in DB
	 * @param classId Id of targeted class
	 */
	class ClassById(classId: Int) extends ItemAccess[existing.Class](classId, Class.factory)
	{
		// COMPUTED	-------------------
		
		private def attributeFactory = model.Attribute
		private def classAttributeCondition = attributeFactory.withClassId(classId).toCondition &&
			attributeFactory.nonDeprecatedCondition
		
		/**
		 * @return An access point to individual attributes that belong to this class
		 */
		def attribute = Attribute
		
		/**
		 * @return An access point to all attributes that belong to this class
		 */
		def attributes = Attributes
		
		
		// NESTED	-------------------
		
		/**
		 * Provides access to individual class attributes
		 */
		object Attribute extends ConditionalSingleAccess[Attribute]
		{
			// IMPLEMENTED	-----------
			
			override def condition = classAttributeCondition
			
			override def factory = attributeFactory
			
			
			// OTHER	--------------
			
			/**
			 * @param id Attribute id
			 * @return An access point to that specific attribute
			 */
			def apply(id: Int) = new AttributeById(id)
			
			
			// NESTED	--------------
			
			/**
			 * Provides access to an individual attribute's data
			 * @param attId Id of targeted attribute
			 */
			class AttributeById(attId: Int) extends ItemAccess[Attribute](attId, attributeFactory)
			{
				// COMPUTED	----------
				
				/**
				 * @return An access point to this attribute's current configuration
				 */
				def configuration = Configuration
				
				/**
				 * Used for accessing the current configuration of an attribute
				 */
				object Configuration extends ConditionalSingleAccess[AttributeConfiguration]
				{
					// IMPLEMENTED	-------
					
					override def condition = AttributeById.this.condition && factory.nonDeprecatedCondition
					
					override def factory = model.AttributeConfiguration
					
					
					// OTHER	-----------
					
					/**
					 * Updates the current configuration of this attribute
					 * @param newConfig New configuration for this attribute
					 * @param connection DB Connection (implicit)
					 * @return Newly inserted configuration with id data
					 */
					def update(newConfig: NewAttributeConfiguration)(implicit connection: Connection) =
					{
						// Deprecates the existing configuration
						connection(factory.deprecatedNow.toUpdateStatement() +
							Where(factory.withAttributeId(attId).toCondition && factory.nonDeprecatedCondition))
						// Inserts a new configuration
						val configurationId = factory.forInsert(attId, newConfig).insert().getInt
						newConfig.withId(configurationId, attId)
					}
				}
			}
		}
		
		object Attributes extends ConditionalManyAccess[Attribute]
		{
			// IMPLEMENTED	-----------
			
			override def condition = classAttributeCondition
			
			override def factory = attributeFactory
			
			
			// OTHER	---------------
			
			/**
			 * Inserts a new attribute for this class to the database
			 * @param newAttribute New attribute to insert
			 * @param connection DB Connection (implicit)
			 * @return Newly inserted attribute
			 */
			def insert(newAttribute: NewAttribute)(implicit connection: Connection) =
			{
				// Inserts the attribute first
				val attId = factory.forInsert(classId).insert().getInt
				// Then inserts the configuration
				val configurationId = model.AttributeConfiguration.forInsert(attId, newAttribute.configuration).insert().getInt
				newAttribute.withId(attId, classId, configurationId)
			}
		}
	}
}
