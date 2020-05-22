package dbd.core.util

import dbd.core.model.enumeration.AttributeType._
import dbd.core.model.existing.mysql.{Column, ForeignKey, Index, Table}
import utopia.flow.util.CollectionExtensions._

import scala.collection.immutable.VectorBuilder

/**
 * Writes table creation SQL
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
object SqlWriter
{
	// OTHER	--------------------------
	
	/**
	 * Creates a database creation SQL statement
	 * @param databaseName Name of the new database
	 * @param tables Tables written to the database
	 * @return Database and table creation statement
	 */
	def apply(databaseName: String, tables: Vector[Table]) =
	{
		// Orders the tables so that tables that link to other tables will always be created after those tables
		val tablesInOrder = orderTablesForCreation(tables)
		val tablesById = tables.map { t => t.id -> t }.toMap
		
		// Writes table sql
		val sql = new StringBuilder
		
		sql ++= s"CREATE DATABASE $databaseName;\n"
		sql ++= s"USE DATABASE $databaseName;"
		
		tablesInOrder.foreach { table =>
			sql ++= "\n\n"
			sql ++= tableToSql(table, tablesById)
		}
		
		sql.result()
	}
	
	/**
	  * Writes a create table statement
	  * @param table Table to create
	  * @param tablesById All tables in database, mapped by table id
	  * @return Create table sql statement
	  */
	def tableToSql(table: Table, tablesById: Map[Int, Table]) =
	{
		val sql = new StringBuilder
		// Inserts table declaration
		sql ++= "CREATE TABLE `"
		sql ++= table.name
		sql ++= "`(\n\t`id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT, "
		
		// Inserts column lines
		columnLines(table).foreach { line =>
			sql ++= "\n\t"
			sql ++= line
			sql ++= ", "
		}
		
		// Inserts add or update column
		if (table.allowsUpdates)
			sql ++= "\n\t`updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
		else
			sql ++= "\n\t`created` TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP"
		
		// Inserts deprecated column if needed
		if (table.usesDeprecation)
			sql ++= ", \n\t`deprecated_after` DATETIME"
		
		// Inserts possible indices
		val idxLines = indexLines(table)
		if (idxLines.nonEmpty)
		{
			sql ++= ", \n\n\t"
			sql ++= idxLines.mkString(", \n\t")
		}
		
		// Inserts deprecation index, if needed
		if (table.usesDeprecation)
			sql ++= s", \n\n\tINDEX ${table.name}_deprecation_idx (`deprecated_after`)"
		
		// Inserts possible foreign keys
		val fkLines = foreignKeyLines(table, tablesById)
		if (fkLines.nonEmpty)
		{
			sql ++= ", \n\n\t"
			sql ++= fkLines.mkString(", \n\t")
		}
		
		// Finishes table
		sql ++= "\n)Engine=InnoDB DEFAULT CHARSET=latin1;"
		
		sql.result()
	}
	
	private def columnLines(table: Table) =
	{
		// First adds reference column lines, then index column lines and finally remaining column lines
		table.columns.sortedWith(Ordering.by { !_.hasForeignKey }, Ordering.by { !_.hasIndex },
			Ordering.by { _.allowsNull }, Ordering.by { _.dataType.id }, Ordering.by { _.name }).map(columnToSql)
	}
	
	private def indexLines(table: Table) =
	{
		table.columns.filter { _.hasIndex }.sortBy { _.index.get.name }.map { c => indexToSql(c.index.get, c.name) }
	}
	
	private def foreignKeyLines(table: Table, tablesById: Map[Int, Table]) =
	{
		table.columns.filter { _.hasForeignKey }.sortBy { _.foreignKey.get.baseName }.map { column =>
			val fk = column.foreignKey.get
			tablesById.get(fk.targetTableId) match
			{
				case Some(targetTable) => fkToSql(fk, column, targetTable.name)
				case None => s"-- WARNING: Couldn't write foreign key from column ${column.name}"
			}
		}
	}
	
	/**
	  * Converts a foreign key to sql
	  * @param fk Foreign key to convert
	  * @param column Column associated with the key (in origin table)
	  * @param targetTableName Name of the targeted table
	  * @return Foreign key sql
	  */
	def fkToSql(fk: ForeignKey, column: Column, targetTableName: String) =
		s"CONSTRAINT ${fk.constraintName} FOREIGN KEY ${fk.indexName} (`${
			column.name}`) REFERENCES `$targetTableName`(`id`) ON DELETE ${if (column.allowsNull) "SET NULL" else "CASCADE"}"
	
	/**
	  * Converts an index to sql
	  */
	def indexToSql(index: Index, columnName: String) = s"INDEX ${index.name} (`$columnName`)"
	
	/**
	  * @return An sql representation of the specified column
	  */
	def columnToSql(column: Column) = s"`${column.name}` ${dataTypeToSql(column)}${if (column.allowsNull) "" else " NOT NULL"}"
	
	/**
	  * Converts a column's data type to sql
	  * @param column Targeted column
	  * @return Sql representation of that column's desired data type
	  */
	def dataTypeToSql(column: Column) = column.dataType match
	{
		case ShortStringType => s"VARCHAR(${if (column.hasIndex) 64 else 255})"
		case IntType => "INT"
		case DoubleType => "DOUBLE"
		case BooleanType => "BOOLEAN"
		case InstantType => "DATETIME" // Timestamp type is already reserved for the 'created' column
	}
	
	/**
	  * Orders specified tables so that they can be created without foreign key problems
	  * @param tables Tables to order
	  * @return Ordered tables
	  */
	def orderTablesForCreation(tables: Vector[Table]) =
	{
		var ordered = Vector[Table]()
		var next = tables
		
		while (next.nonEmpty)
		{
			val rejectedBuilder = new VectorBuilder[Table]
			
			// Tries to place tables. Only possible if all referenced tables have been added already
			next.foreach { table =>
				if (table.referencedTableIds.forall { referencedId => ordered.exists { _.id == referencedId } })
					ordered :+= table
				else
					rejectedBuilder += table
			}
			
			// Repeats until all tables have been placed
			// TODO: Handle cases where trying to add tables with circular references (add foreign keys separately?)
			val nextIteration = rejectedBuilder.result()
			if (nextIteration.size < next.size)
				next = nextIteration
			else
			{
				Log.warning(s"Couldn't order following tables in a way where foreign keys can be respected: [${nextIteration.map { _.name }.mkString(", ")}]")
				ordered :+= nextIteration.head
				next = nextIteration.tail
			}
		}
		
		ordered
	}
}
