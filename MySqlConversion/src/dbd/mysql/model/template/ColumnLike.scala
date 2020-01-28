package dbd.mysql.model.template

import dbd.core.model.enumeration.AttributeType

/**
 * A common trait for columns
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
trait ColumnLike[+Index <: IndexLike]
{
	/**
	 * @return If of linked attribute
	 */
	def attributeId: Int
	/**
	 * @return Name of this column
	 */
	def name: String
	/**
	 * @return Data type of this column
	 */
	def dataType: AttributeType
	/**
	 * @return Whether this column allows null values
	 */
	def allowsNull: Boolean
	/**
	 * @return An index associated with this column. None if no index is attached.
	 */
	def index: Option[Index]
}
