package dbd.core.model.template

/**
 * A common trait for database models
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
@deprecated("This will be removed during beta", "v2")
trait DatabaseConfigurationLike
{
	/**
	 * @return Name of this database
	 */
	def name: String
}
