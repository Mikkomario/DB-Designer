package dbd.core.model.template

/**
 * A common trait for link implementations
 * @author Mikko
 * @since 19.1.2020, v
 */
trait LinkLike
{
	// ABSTRACT	----------------------
	
	/**
	 * @return The current configuration of this link
	 */
	def configuration: LinkConfigurationLike
	
	
	// COMPUTED	----------------------
	
	/**
	 * @return The current type for this link
	 */
	def linkType = configuration.linkType
	
	/**
	 * @return Id of class this link targets
	 */
	def targetClassId = configuration.targetClassId
	
	/**
	 * @return Id of class this link originates from
	 */
	def originClassId = configuration.originClassId
}
