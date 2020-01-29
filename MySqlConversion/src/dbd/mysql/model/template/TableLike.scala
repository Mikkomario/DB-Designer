package dbd.mysql.model.template

/**
 * A common trait for tables
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
trait TableLike[+Index <: IndexLike, FK <: ForeignKeyLike, AL <: ColumnAttributeLinkLike[Index],
	LL <: ColumnLinkLinkLike[FK], +Column <: ColumnLike[Index, FK, AL, LL]]
{
	// ABSTRACT	--------------------------
	
	/**
	 * @return Id of the class this table is linked to
	 */
	def classId: Int
	/**
	 * @return Name of this table
	 */
	def name: String
	/**
	 * @return Whether this table uses deprecation style
	 */
	def usesDeprecation: Boolean
	/**
	 * @return Whether this table allows column updates (deprecation doesn't count)
	 */
	def allowsUpdates: Boolean
	/**
	 * @return Columns in this table
	 */
	def columns: Vector[Column]
	
	
	// COMPUTED	-------------------------
	
	/**
	 * @return Indices in this table
	 */
	def indices = columns.flatMap { _.index }
	
	/**
	 * @return All foreign keys in this table
	 */
	def foreignKeys = columns.flatMap { _.foreignKey }
	
	/**
	 * @return The ids of the other tables referenced from this table
	 */
	def referencedTableIds = foreignKeys.map { _.targetTableId }.toSet
	
	
	// OTHER	-------------------------
	
	/**
	 * @param targetTableId Id of another table
	 * @return Whether this table contains references to the specified table
	 */
	def containsReferencesToTableWithId(targetTableId: Int) = foreignKeys.exists { _.targetTableId == targetTableId }
}
