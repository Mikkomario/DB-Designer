package dbd.core.model.template

/**
 * Common trait for foreign keys
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
trait ForeignKeyLike
{
	// ABSTRACT	-----------------------
	
	/**
	 * @return Name of this foreign key without anything added yet
	 */
	def baseName: String
	/**
	 * @return Id of the table this foreign key points to
	 */
	def targetTableId: Int
	
	
	// COMPUTED	----------------------
	
	/**
	  * @return Name of this foreign key when used as an index
	  */
	def indexName = baseName + "_idx"
	
	/**
	  * @return Name of this foreign key when used as a constraint
	  */
	def constraintName = baseName + "_fk"
}
