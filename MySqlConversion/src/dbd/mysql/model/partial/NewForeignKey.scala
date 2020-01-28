package dbd.mysql.model.partial

import dbd.mysql.model.template.ForeignKeyLike

/**
 * Used for adding new foreign key data to DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class NewForeignKey(originColumnId: Int, targetTableId: Int, baseName: String) extends ForeignKeyLike
