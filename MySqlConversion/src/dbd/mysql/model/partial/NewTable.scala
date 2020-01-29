package dbd.mysql.model.partial

import dbd.mysql.model.template.TableLike

/**
 * Represents a table before it is stored to DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class NewTable(classId: Int, name: String, usesDeprecation: Boolean, allowsUpdates: Boolean, columns: Vector[NewColumn])
	extends TableLike[NewIndex, NewForeignKey, NewColumnAttributeLink, NewColumnLinkLink, NewColumn]
