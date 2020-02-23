package dbd.mysql.model.change

import dbd.mysql.controller.SqlWriter
import utopia.flow.util.CollectionExtensions._
import dbd.mysql.model.existing.Table

/**
  * Compares two versions of the same table in different releases (connected via class id)
  * @author Mikko Hilpinen
  * @since 23.2.2020, v0.1
  */
case class TableComparison(classId: Int, oldVersion: Table, newVersion: Table)
{
	// ATTRIBUTES	------------------------------
	
	private val (oldAttributeColumns, oldLinkColumns) = oldVersion.columns.divideBy { _.linkedData.isLeft }
	private val (newAttributeColumns, newLinkColumns) = newVersion.columns.divideBy { _.linkedData.isLeft }
	
	private val (removedAttColumns, comparableAttColumns, addedAttColumns) = oldAttributeColumns.listChanges(
		newAttributeColumns) { _.linkedData.rightOption.get.attributeId } { case (attId, o, n) =>
		AttributeColumnComparison(attId, o, n) }
	private val (removedLinkColumns, comparableLinkColumns, addedLinkColumns) = oldLinkColumns.listChanges(newLinkColumns) {
		_.linkedData.leftOption.get.linkId } { case (linkId, o, n) => LinkColumnComparison(linkId, o, n) }
	
	
	// COMPUTED	----------------------------------
	
	/**
	  * @return Changed table names (old -> new) if there was a change
	  */
	def nameChange =
	{
		val oldName = oldVersion.name
		val newName = newVersion.name
		
		if (oldName != newName)
			Some(oldName -> newName)
		else
			None
	}
	
	/**
	  * @return Sql statement for applying this table renaming
	  */
	def nameChangeSql = nameChange.map { case (oldName, newName) => s"RENAME TABLE `$oldName` TO `$newName`;" }
	
	/**
	  * @return Sql statement for altering the changed columns. None if there was no change in columns.
	  */
	def alterColumnsSql = toAlterTableSql(comparableAttColumns.flatMap { _.toAlterTableSql } ++
		comparableLinkColumns.flatMap { _.toAlterTableSql })
	
	/**
	  * @return Sql statement for adding new columns. None if there were no new columns.
	  */
	def newColumnsSql = toAlterTableSql((addedAttColumns ++ addedLinkColumns).map { c =>
		s"ADD ${SqlWriter.columnToSql(c)}" })
	
	/**
	  * @return Sql statement for removing old columns. None if no columns should be removed.
	  */
	def removedColumnsSql = toAlterTableSql((removedAttColumns ++ removedLinkColumns).map { c =>
		s"DROP `${c.name}`" })
	
	private def toAlterTableSql(lines: Seq[String]) =
	{
		if (lines.nonEmpty)
			Some(s"ALTER TABLE `${newVersion.name}`\n\t${lines.mkString(",\n\t")};")
		else
			None
	}
}
