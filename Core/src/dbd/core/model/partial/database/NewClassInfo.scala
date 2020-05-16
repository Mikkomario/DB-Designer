package dbd.core.model.partial.database

import dbd.core.model.existing.database.ClassInfo
import dbd.core.model.template.ClassInfoLike

/**
 * Represents class info before it is saved
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
case class NewClassInfo(name: String, isMutable: Boolean = false) extends ClassInfoLike
{
	/**
	 * @param id Id for this class info
	 * @param classId Id of targeted class
	 * @return A copy of this model with id data
	 */
	def withId(id: Int, classId: Int) = ClassInfo(id, classId, name, isMutable)
}
