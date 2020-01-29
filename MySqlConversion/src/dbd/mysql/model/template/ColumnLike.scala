package dbd.mysql.model.template

import dbd.core.model.enumeration.AttributeType

/**
 * A common trait for columns
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
trait ColumnLike[+Index <: IndexLike, FK <: ForeignKeyLike, AL <: ColumnAttributeLinkLike[Index], LL <: ColumnLinkLinkLike[FK]]
{
	/**
	 * @return Either linked attribute data or link data
	 */
	def linkedData: Either[LL, AL]
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
}
