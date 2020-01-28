package dbd.mysql.model.partial

import dbd.mysql.model.template.IndexLike

/**
 * Represents an index before it is stored to DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class NewIndex(name: String) extends IndexLike
