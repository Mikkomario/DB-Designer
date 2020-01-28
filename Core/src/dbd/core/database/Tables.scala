package dbd.core.database

import dbd.core.util.ThreadPool
import utopia.vault.model.immutable.Table

/**
 * Used for accessing tables used in DB Designer project
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 */
object Tables extends utopia.vault.database.Tables(ConnectionPool)(ThreadPool.executionContext)
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
	
	/**
	 * @param tableName Name of targeted table
	 * @return a cached table
	 */
	def apply(tableName: String): Table = apply(dbName, tableName)
}
