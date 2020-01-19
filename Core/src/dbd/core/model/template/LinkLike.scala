package dbd.core.model.template

/**
 * A common trait for link implementations
 * @author Mikko
 * @since 19.1.2020, v
 */
trait LinkLike
{
	/**
	 * @return The current configuration of this link
	 */
	def configuration: LinkConfigurationLike
}
