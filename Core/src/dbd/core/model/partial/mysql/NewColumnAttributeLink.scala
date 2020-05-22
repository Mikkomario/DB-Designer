package dbd.core.model.partial.mysql

import dbd.core.model.existing.database.AttributeConfiguration
import dbd.core.model.template.ColumnAttributeLinkLike

/**
 * Represents a connection between a column and an attribute before one is stored to DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class NewColumnAttributeLink(attributeConfiguration: AttributeConfiguration, index: Option[NewIndex])
	extends ColumnAttributeLinkLike[NewIndex]
