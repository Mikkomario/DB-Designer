package dbd.core.model.partial.mysql

import dbd.core.model.existing.database.LinkConfiguration
import dbd.core.model.template.ColumnLinkLinkLike

/**
 * Represents a connection between a new column and a link
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class NewColumnLinkLink(linkConfiguration: LinkConfiguration, foreignKey: NewForeignKey) extends ColumnLinkLinkLike[NewForeignKey]
