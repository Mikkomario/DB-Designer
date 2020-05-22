package dbd.core.model.template

import dbd.core.model.existing.database.AttributeConfiguration

/**
 * A common trait for models that link columns and attributes
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
trait ColumnAttributeLinkLike[+Index]
{
	// ABSTRACT	-------------------------
	
	/**
	 * @return Attribute configuration this column is based on
	 */
	def attributeConfiguration: AttributeConfiguration
	/**
	 * @return A possible index linked to this column
	 */
	def index: Option[Index]
	
	
	// COMPUTED	------------------------
	
	/**
	  * @return Id of the attribute associated with this column
	  */
	def attributeId: Int = attributeConfiguration.attributeId
}
