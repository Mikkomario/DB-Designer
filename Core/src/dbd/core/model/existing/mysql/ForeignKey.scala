package dbd.core.model.existing.mysql

import dbd.core.model.template.ForeignKeyLike

/**
 * Represents a foreign key that already exists in DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class ForeignKey(id: Int, targetTableId: Int, baseName: String) extends ForeignKeyLike
