package dbd.mysql.model.partial

import dbd.mysql.model.template.ColumnAttributeLinkLike

/**
 * Represents a connection between a column and an attribute before one is stored to DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class NewColumnAttributeLink(attributeId: Int, index: Option[NewIndex]) extends ColumnAttributeLinkLike[NewIndex]
