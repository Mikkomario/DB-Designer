package dbd.core.model.partial.mysql

import dbd.core.model.existing.mysql.ForeignKey
import dbd.core.model.template.ForeignKeyLike

/**
 * Used for adding new foreign key data to DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class NewForeignKey(targetTableId: Int, baseName: String) extends ForeignKeyLike
{
	/**
	 * @param id New id for this model
	 * @return A copy of this model with id attached
	 */
	def withId(id: Int) = ForeignKey(id, targetTableId, baseName)
}
