package dbd.mysql.model.template

/**
 * A common trait for database models
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
trait DatabaseConfigurationLike
{
	/**
	 * @return Id of described database
	 */
	def databaseId: Int
	/**
	 * @return Name of this database
	 */
	def name: String
}
