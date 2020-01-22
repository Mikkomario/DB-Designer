package dbd.core.model.existing

import dbd.core.model.template.ClassInfoLike

/**
 * Specifies the name and type of a class
 * @author Mikko Hilpinen
 * @since 10.1.2020, v0.1
 * @param name The name of the class
 * @param isMutable Whether the class attributes are mutable
 */
case class ClassInfo(id: Int, classId: Int, name: String, isMutable: Boolean) extends ClassInfoLike
{
	override def toString = s"$name${if (isMutable) " (mutable)" else ""}"
}
