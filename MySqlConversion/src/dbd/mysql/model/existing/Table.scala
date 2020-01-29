package dbd.mysql.model.existing

import dbd.mysql.model.template.TableLike

/**
 * Represents a table stored in DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Table(id: Int, releaseId: Int, classId: Int, name: String, usesDeprecation: Boolean, columns: Vector[Column])
	extends TableLike[Index, ForeignKey, ColumnAttributeLink, ColumnLinkLink, Column]
