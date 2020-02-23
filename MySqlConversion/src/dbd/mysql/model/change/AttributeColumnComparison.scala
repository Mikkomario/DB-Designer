package dbd.mysql.model.change

import dbd.mysql.controller.SqlWriter
import dbd.mysql.model.existing.Column

/**
  * Compares two versions of a single attribute column in two different releases (connected via attribute id)
  * @author Mikko Hilpinen
  * @since 23.2.2020, v0.1
  */
case class AttributeColumnComparison(attributeId: Int, oldVersion: Column, newVersion: Column)
{
	/**
	  * @return Whether the column definition has changed somehow
	  */
	def hasChanged = oldVersion.name != newVersion.name || oldVersion.dataType != newVersion.dataType ||
		oldVersion.allowsNull != newVersion.allowsNull
	
	/**
	  * @return An alter table line (doesn't contain the alter table part) for column type change (None if not changed)
	  */
	def toAlterTableSql =
	{
		if (hasChanged)
			Some(s"CHANGE `${oldVersion.name}` ${SqlWriter.columnToSql(newVersion)}")
		else
			None
	}
}
