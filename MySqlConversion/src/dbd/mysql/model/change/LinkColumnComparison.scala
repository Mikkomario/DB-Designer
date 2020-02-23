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
}
