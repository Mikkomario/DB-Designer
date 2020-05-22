package dbd.core.model.combined.mysql

import dbd.core.model.existing.mysql.Table
import dbd.core.util.SqlWriter
import utopia.flow.util.CollectionExtensions._

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
	// TODO: Throws if this comparison's class id is not part of the linked ids
	private val (removedLinkColumns, comparableLinkColumns, addedLinkColumns) = oldLinkColumns.listChanges(newLinkColumns) {
		_.linkedData.leftOption.get.linkConfiguration.oppositeClassId(classId).get } { case (linkId, o, n) =>
		LinkColumnComparison(linkId, o, n) }
	
	
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
	
	/**
	  * @return Sql statement for adding new indices (based on column changes)
	  */
	def newIndicesSql =
	{
		val newIndices = comparableAttColumns.flatMap { c =>
			c.addedIndex.map { c.newVersion -> _ } } ++ addedAttColumns.flatMap { c => c.index.map { c -> _ } }
		toAlterTableSql(newIndices.map { case (c, i) => s"ADD ${SqlWriter.indexToSql(i, c.name)}" })
	}
	
	/**
	  * @return Sql statement for removing old indices (based on column changes)
	  */
	def removedIndicesSql =
	{
		val removedIndices = comparableAttColumns.flatMap { _.removedIndex } ++ removedAttColumns.flatMap { _.index }
		toAlterTableSql(removedIndices.map { i => s"DROP INDEX ${i.name}" })
	}
	
	/**
	  * @param tablesForIds All tables matched with their ids
	  * @return Alter table statement for adding new foreign keys to this table. None if not changed.
	  */
	def newForeignKeysSql(tablesForIds: Map[Int, Table]) =
	{
		val newFks = comparableLinkColumns.flatMap { c => c.newForeignKey(tablesForIds).map { c.newVersion -> _ } } ++
			addedLinkColumns.flatMap { c => c.foreignKey.map { c -> _ } }
		toAlterTableSql(newFks.map { case (c, fk) =>
			s"ADD ${SqlWriter.fkToSql(fk, c, tablesForIds(fk.targetTableId).name)}" })
	}
	
	/**
	  * @return Alter table statement for removing old foreign keys from this table. None if not changed
	  */
	def removedForeignKeysSql(tablesForIds: Map[Int, Table]) =
	{
		val removedFks = comparableLinkColumns.flatMap { _.removedForeignKey(tablesForIds) } ++
			removedLinkColumns.flatMap { _.foreignKey }
		toAlterTableSql(removedFks.flatMap { fk => Vector(s"DROP FOREIGN KEY ${fk.constraintName}", s"DROP INDEX ${fk.indexName}") })
	}
	
	private def toAlterTableSql(lines: Seq[String]) =
	{
		if (lines.nonEmpty)
			Some(s"ALTER TABLE `${newVersion.name}`\n\t${lines.mkString(",\n\t")};")
		else
			None
	}
}
