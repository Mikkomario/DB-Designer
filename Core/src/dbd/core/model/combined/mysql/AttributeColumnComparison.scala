package dbd.core.model.combined.mysql

import dbd.core.model.existing.mysql.Column
import dbd.core.util.SqlWriter

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
	
	/**
	  * @return Index added in this change
	  */
	def addedIndex = newVersion.index.flatMap { newIndex =>
		oldVersion.index match
		{
			case Some(oldIndex) => if (newIndex.name == oldIndex.name) None else Some(newIndex)
			case None => Some(newIndex)
		}
	}
	
	/**
	  * @return Index removed in this change
	  */
	def removedIndex = oldVersion.index.flatMap { oldIndex =>
		newVersion.index match
		{
			case Some(newIndex) => if (newIndex.name == oldIndex.name) None else Some(oldIndex)
			case None => Some(oldIndex)
		}
	}
}
