package dbd.mysql.model.existing

import dbd.core.model.enumeration.AttributeType
import dbd.mysql.model.template.ColumnLike

/**
 * Represents a recorded column
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Column(id: Int, tableId: Int, linkedData: Either[ColumnLinkLink, ColumnAttributeLink], name: String,
				  dataType: AttributeType, allowsNull: Boolean)
	extends ColumnLike[Index, ForeignKey, ColumnAttributeLink, ColumnLinkLink]
