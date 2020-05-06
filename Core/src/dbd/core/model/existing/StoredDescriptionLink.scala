package dbd.core.model.existing

import dbd.core.model.template.DescriptionLinkLike

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
}
