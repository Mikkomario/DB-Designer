package dbd.core.model.template

/**
  * Common trait for description links
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
trait DescriptionLinkLike[+D]
{
	/**
	  * @return Id of the description target
	  */
	def targetId: Int
	/**
	  * @return Description of the target
	  */
	def description: D
}
