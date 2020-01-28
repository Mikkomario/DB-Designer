package dbd.mysql.model.existing

import dbd.mysql.model.template.IndexLike

/**
 * Represents an index stored in DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Index(id: Int, columnId: Int, name: String) extends IndexLike
