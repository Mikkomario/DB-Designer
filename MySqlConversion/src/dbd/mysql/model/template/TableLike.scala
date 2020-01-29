package dbd.mysql.model.template

/**
 * A common trait for tables
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
trait TableLike[+Index <: IndexLike, FK <: ForeignKeyLike, AL <: ColumnAttributeLinkLike[Index],
	LL <: ColumnLinkLinkLike[FK], +Column <: ColumnLike[Index, FK, AL, LL]]
{
	/**
	 * @return Id of the class this table is linked to
	 */
	def classId: Int
	/**
	 * @return Name of this table
	 */
	def name: String
	/**
	 * @return Columns in this table
	 */
	def columns: Vector[Column]
}
