package dbd.mysql.model.existing

import dbd.mysql.model.template.ColumnLike
import utopia.flow.generic.DataType

/**
 * Represents a recorded column
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Column(id: Int, tableId: Int, attributeId: Int, name: String, dataType: DataType,
				  allowsNull: Boolean, index: Option[Index], foreignKey: Option[ForeignKey]) extends ColumnLike[Index]
