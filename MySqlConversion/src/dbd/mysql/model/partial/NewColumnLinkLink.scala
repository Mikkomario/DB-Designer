package dbd.mysql.model.partial

import dbd.mysql.model.template.ColumnLinkLinkLike

/**
 * Represents a connection between a new column and a link
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class NewColumnLinkLink(linkConfigurationId: Int, foreignKey: NewForeignKey) extends ColumnLinkLinkLike[NewForeignKey]
