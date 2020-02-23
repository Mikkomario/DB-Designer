package dbd.mysql.model.change

import dbd.mysql.model.existing.Column

/**
  * Compares two versions of a link column
  * @author Mikko Hilpinen
  * @since 23.2.2020, v0.1
  */
case class LinkColumnComparison(linkId: Int, oldVersion: Column, newVersion: Column)
{
	/**
	  * @return An sql segment for altering column properties. To be used in ALTER TABLE -statement. None if not changed.
	  */
	def toAlterTableSql = if (oldVersion.name == newVersion.name) None else
		Some(s"CHANGE `${oldVersion.name}` TO `${newVersion.name}`")
	
	/**
	  * @return Foreign key added in this change
	  */
	def newForeignKey = newVersion.foreignKey.flatMap { fk =>
		oldVersion.foreignKey match
		{
			case Some(oldFk) => if (fk.targetTableId == oldFk.targetTableId && fk.baseName == oldFk.baseName) None else Some(fk)
			case None => Some(fk)
		}
	}
	
	/**
	  * @return Foreign key removed in this change
	  */
	def removedForeignKey = oldVersion.foreignKey.flatMap { fk =>
		newVersion.foreignKey match
		{
			case Some(newFk) => if (newFk.targetTableId == fk.targetTableId && newFk.baseName == fk.baseName) None else Some(fk)
			case None => Some(fk)
		}
	}
}
