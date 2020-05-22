package dbd.core.model.template

import dbd.core.model.enumeration.AttributeType
import utopia.flow.util.CollectionExtensions._

/**
 * A common trait for columns
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
trait ColumnLike[+Index <: IndexLike, FK <: ForeignKeyLike, AL <: ColumnAttributeLinkLike[Index], LL <: ColumnLinkLinkLike[FK]]
{
	// ABSTRACT	----------------------
	
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
	
	
	// COMPUTED	-----------------------
	
	/**
	 * @return Whether this column is used as an index
	 */
	def hasIndex = linkedData.rightOption.exists { _.index.isDefined }
	
	/**
	 * @return Whether this column has an associated foreign key
	 */
	def hasForeignKey = linkedData.isLeft
	
	/**
	 * @return An index associated with this column, if there is one
	 */
	def index = linkedData.rightOption.flatMap { _.index }
	
	/**
	 * @return A foreign key associated with this column, if there is one
	 */
	def foreignKey = linkedData.leftOption.map { _.foreignKey }
}
