package dbd.core.model.existing

import dbd.core.model.template.DescriptionLinkLike
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.generic.ValueConversions._

/**
  * A common trait for stored description link variants
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
trait StoredDescriptionLink[+Data <: DescriptionLinkLike[Description]] extends Stored[Data]
	with DescriptionLinkLike[Description]
{
	override def targetId = data.targetId
	
	override def description = data.description
	
	override def toModel = super.toModel + Constant("link_id", id)
}
