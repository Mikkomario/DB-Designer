package dbd.mysql.model.template

/**
 * Common trait for index models
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
trait IndexLike
{
	/**
	 * @return Name of this index
	 */
	def name: String
}
