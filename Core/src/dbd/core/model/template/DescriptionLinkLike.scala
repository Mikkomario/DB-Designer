package dbd.core.model.template

import utopia.flow.datastructure.immutable.Constant
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Common trait for description links
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
trait DescriptionLinkLike[+D <: ModelConvertible] extends ModelConvertible
{
	// ABSTRACT	--------------------------
	
	/**
	  * @return Id of the description target
	  */
	def targetId: Int
	/**
	  * @return Description of the target
	  */
	def description: D
	
	
	// IMPLEMENTED	----------------------
	
	override def toModel = description.toModel + Constant("target_id", targetId)
}
