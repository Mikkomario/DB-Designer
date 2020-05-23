package dbd.core.model.existing.mysql

import dbd.core.model.existing.database.LinkConfiguration
import dbd.core.model.template.ColumnLinkLinkLike

/**
 * Represents a stored connection between a column and a link
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class ColumnLinkLink(id: Int, columnId: Int, linkConfiguration: LinkConfiguration, foreignKey: ForeignKey)
	extends ColumnLinkLinkLike[ForeignKey]