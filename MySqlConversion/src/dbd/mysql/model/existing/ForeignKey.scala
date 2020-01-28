package dbd.mysql.model.existing

import dbd.mysql.model.template.ForeignKeyLike

/**
 * Represents a foreign key that already exists in DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class ForeignKey(id: Int, originColumnId: Int, targetTableId: Int, baseName: String) extends ForeignKeyLike
