package dbd.mysql.model.change

import dbd.core.util.Log
import dbd.mysql.controller.SqlWriter
import utopia.flow.util.CollectionExtensions._
import dbd.mysql.model.existing.Table

object ReleasesComparison
{
	/**
	  * Creates a new release comparison by comparing two sets of tables
	  * @param databaseId Id of targeted database
	  * @param oldTables Old set of tables
	  * @param newTables New set of tables
	  * @return A comparison between the two database states
	  */
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
	// ATTRIBUTES	------------------------
	
	private val tablesById = (newTables ++ modifiedTables.map { _.newVersion }).map { t => t.id -> t }.toMap
	
	
	// COMPUTED	----------------------------
	
	/**
	  * Creates an sql containing all changes between compared versions
	  * @param databaseName Name of the affected database
	  * @return A set of sql statements that update table structure from previous release to new release
	  */
	def toSql(databaseName: String, oldVersionName: String, newVersionName: String) =
	{
		val sql = new StringBuilder
		
		sql ++= s"--\n-- Updating $databaseName from $oldVersionName to $newVersionName\n--"
		sql ++= s"\n\nUSE $databaseName\n"
		
		// Performs the changes in specific order
		appendStatements("Renames tables", tableRenameSqlLines, sql)
		appendStatements("Removes old foreign keys", removeOldForeignKeysStatements, sql)
		appendStatements("Removes unused tables", removeTablesSqlLines, sql)
		appendStatements("Removes unused indices", removeOldIndicesStatements, sql)
		appendStatements("Removes unused columns", removeOldColumnsStatements, sql)
		appendStatements("Adds new tables", newTablesSqlStatements, sql)
		appendStatements("Modifies changed columns", alterColumnsSqlStatements, sql)
		appendStatements("Adds missing columns", addNewColumnsStatements, sql)
		appendStatements("Adds missing indices", addNewIndicesStatements, sql)
		appendStatements("Adds missing foreign keys", addNewForeignKeysStatements, sql)
		
		sql.result()
	}
	
	private def appendStatements(description: => String, statements: Vector[String], builder: StringBuilder) =
	{
		if (statements.nonEmpty)
		{
			builder ++= "\n-- "
			builder ++= description
			builder ++= "\n"
			statements.foreach { s =>
				builder ++= s
				builder ++= "\n"
			}
		}
	}
	
	private def tableRenameSqlLines = modifiedTables.flatMap { _.nameChangeSql }
	
	private def newTablesSqlStatements = SqlWriter.orderTablesForCreation(newTables).map {
		SqlWriter.tableToSql(_, tablesById) }
	
	// Removed tables need to be in specific order, so that tables that refer to other tables are removed first
	private def removeTablesSqlLines = orderTablesForDeletion(removedTables).map { t => s"DROP TABLE `${t.name}`;" }
	
	private def alterColumnsSqlStatements = modifiedTables.flatMap { _.alterColumnsSql }
	
	private def addNewColumnsStatements = modifiedTables.flatMap { _.newColumnsSql }
	
	private def removeOldColumnsStatements = modifiedTables.flatMap { _.removedColumnsSql }
	
	private def addNewIndicesStatements = modifiedTables.flatMap { _.newIndicesSql }
	
	private def removeOldIndicesStatements = modifiedTables.flatMap { _.removedIndicesSql }
	
	private def addNewForeignKeysStatements = modifiedTables.flatMap { _.newForeignKeysSql(tablesById) }
	
	private def removeOldForeignKeysStatements = modifiedTables.flatMap { _.removedForeignKeysSql }
	
	private def orderTablesForDeletion(tables: Vector[Table]): Vector[Table] =
	{
		if (tables.size < 2)
			tables
		else
		{
			// Needs to remove tables in order where those referencing other tables are removed first
			// TODO: Handle circular references
			tables.indexWhereOption { t1 => !tables.exists { t2 => t2.containsReferencesToTableWithId(t1.id) && t2 != t1 } } match
			{
				case Some(removedIndex) => tables(removedIndex) +: orderTablesForDeletion(tables.withoutIndex(removedIndex))
				case None =>
					Log.warning(s"Cannot find proper deletion order between following tables: [${tables.map { _.name }}]")
					tables.head +: orderTablesForDeletion(tables.tail)
			}
		}
	}
}
