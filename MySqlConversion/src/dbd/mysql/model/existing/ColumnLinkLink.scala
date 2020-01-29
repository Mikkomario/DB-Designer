package dbd.mysql.model.existing

import dbd.mysql.model.template.ColumnLinkLinkLike

/**
 * Represents a stored connection between a column and a link
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class ColumnLinkLink(id: Int, columnId: Int, linkId: Int, foreignKey: ForeignKey)
	extends ColumnLinkLinkLike[ForeignKey]
