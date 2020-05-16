package dbd.mysql.model.existing

import dbd.core.model.existing.database.AttributeConfiguration
import dbd.mysql.model.template.ColumnAttributeLinkLike

/**
 * Represents a stored connection between a column and an attribute
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class ColumnAttributeLink(id: Int, columnId: Int, attributeConfiguration: AttributeConfiguration, index: Option[Index])
	extends ColumnAttributeLinkLike[Index]
