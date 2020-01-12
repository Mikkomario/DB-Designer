package dbd.core.model.partial

import dbd.core.model.template.ClassLike

/**
 * Represents a class before it is saved
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
case class NewClass(info: NewClassInfo, attributes: Vector[NewAttribute])
	extends ClassLike[NewClassInfo, NewAttribute, NewClass]
{
	override protected def makeCopy(info: NewClassInfo, attributes: Vector[NewAttribute]) = NewClass(info, attributes)
}
