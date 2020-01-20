package dbd.core.database

/**
 * Used for accessing tables used in DB Designer project
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 */
object Tables
{
	// ATTRIBUTES	----------------------
	
	private val dbName = "db_designer"
	
	
	// COMPUTED	--------------------------
	
	/**
	 * @return Table that contains classes
	 */
	def classTable = apply("class")
	
	/**
	 * @return Table that contains class attributes
	 */
	def attribute = apply("attribute")
	
	/**
	 * @return Table that contains base class info
	 */
	def classInfo = apply("class_info")
	
	/**
	 * @return Table that contains configurations for class attributes
	 */
	def attributeConfiguration = apply("attribute_configuration")
	
	/**
	 * @return Table that contains links between classes
	 */
	def link = apply("link")
	
	/**
	 * @return Table that contains configurations for links between classes
	 */
	def linkConfiguration = apply("link_configuration")
	
	
	// OTHER	--------------------------
	
	private def apply(tableName: String) = utopia.vault.database.Tables(dbName, tableName)
}
