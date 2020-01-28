package dbd.mysql.model.partial

import dbd.core.model.enumeration.AttributeType
import dbd.mysql.model.template.ColumnLike

/**
 * Represents a column before it is added to DB
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class NewColumn(attributeId: Int, name: String, dataType: AttributeType, allowsNull: Boolean = false,
					 index: Option[NewIndex] = None) extends ColumnLike[NewIndex]
