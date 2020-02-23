package dbd.mysql.model.template

import dbd.core.model.existing.LinkConfiguration

/**
 * Common trait for models that link columns and links
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
trait ColumnLinkLinkLike[FK <: ForeignKeyLike]
{
	// ABSTRACT	--------------------------
	
	/**
	 * @return Id of associated link
	 */
	def linkConfiguration: LinkConfiguration
	/**
	 * @return Associated foreign key data
	 */
	def foreignKey: FK
	
	
	// COMPUTED	--------------------------
	
	/**
	  * @return Id of the link associated with this column
	  */
	def linkId = linkConfiguration.linkId
}
