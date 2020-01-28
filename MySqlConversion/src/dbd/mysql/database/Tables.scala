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
	 * @return Table that contains registered databases
	 */
	def database = apply("database")
	
	/**
	 * @return Table that contains database configurations
	 */
	def databaseConfiguration = apply("database_configuration")
	
	/**
	 * @return Table that contains database releases
	 */
	def databaseRelease = apply("database_release")
	
	/**
	 * @return Table that contains released tables
	 */
	def table = apply("table_release")
	
	/**
	 * @return Table that contains released column data
	 */
	def column = apply("column_release")
	
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
