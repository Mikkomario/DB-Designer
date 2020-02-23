package dbd.mysql.model.change

import dbd.mysql.controller.SqlWriter
import utopia.flow.util.CollectionExtensions._
import dbd.mysql.model.existing.Table

object ReleasesComparison
{
	def apply(databaseId: Int, oldTables: Vector[Table], newTables: Vector[Table]): ReleasesComparison =
	{
		val (removedTables, modifiedTables, addedTables) = oldTables.listChanges(newTables) { _.classId } {
			case (k, o, n) => TableComparison(k, o, n) }
		ReleasesComparison(databaseId, addedTables, removedTables, modifiedTables)
	}
}

/**
  * Compares the table structure of two releases. Generates changes in Sql format.
  * @author Mikko Hilpinen
  * @since 23.2.2020, v0.1
  */
case class ReleasesComparison private(databaseId: Int, newTables: Vector[Table], removedTables: Vector[Table],
									  modifiedTables: Vector[TableComparison])
{
	// COMPUTED	----------------------------
	
	private def tableRenameSqlLines = modifiedTables.flatMap { _.nameChangeSql }
	
	private def newTablesSqlStatements =
	{
		val tablesById = (newTables ++ modifiedTables.map { _.newVersion }).map { t => t.id -> t }.toMap
		newTables.map { SqlWriter.tableToSql(_, tablesById) }
	}
	
	private def removeTablesSqlLines = removedTables.map { t => s"DROP TABLE `${t.name}`;" }
	
	private def alterColumnsSqlStatements = modifiedTables.flatMap { _.alterColumnsSql }
	
	private def addNewColumnsStatements = modifiedTables.flatMap { _.newColumnsSql }
	
	private def removeOldColumnsStatements = modifiedTables.flatMap { _.removedColumnsSql }
}
