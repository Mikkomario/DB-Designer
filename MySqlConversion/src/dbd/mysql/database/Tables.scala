package dbd.mysql.database

/**
 * Used for accessing db export related tables
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
object Tables
{
	// COMPUTED	----------------------
	
	/**
	 * @return Table that contains database releases
	 */
	def release = apply("database_release")
	
	/**
	 * @return Table that contains released tables
	 */
	def table = apply("table_release")
	
	/**
	 * @return Table that contains released column data
	 */
	def column = apply("column_release")
	
	/**
	 * @return Table that contains links between columns, attributes and indices
	 */
	def columnAttributeLink = apply("column_attribute_link")
	
	/**
	 * @return Table that contains links between columns, links and foreign keys
	 */
	def columnLinkLink = apply("column_link_link")
	
	/**
	 * @return Table that contains released index data
	 */
	def index = apply("index_release")
	
	/**
	 * @return Table that contains released foreign key data
	 */
	def foreignKey = apply("foreign_key_release")
	
	
	// OTHER	----------------------
	
	private def apply(tableName: String) = dbd.core.database.Tables(tableName)
}
