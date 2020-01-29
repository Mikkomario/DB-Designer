package dbd.mysql.controller

import utopia.flow.util.CollectionExtensions._
import dbd.core.model.enumeration.AttributeType._
import dbd.mysql.model.existing.{Column, ForeignKey, Index, Table}

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
		val tablesInOrder = orderTables(tables)
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
	
	private def tableToSql(table: Table, tablesById: Map[Int, Table]) =
	{
		val sql = new StringBuilder
		// Inserts table declaration
		sql ++= "CREATE TABLE `"
		sql ++= table.name
		sql ++= "`(\n\tid INT NOT NULL PRIMARY KEY AUTO_INCREMENT, "
		
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
		
		// Inserts possible indices
		val idxLines = indexLines(table)
		if (idxLines.nonEmpty)
		{
			sql ++= ", \n\n\t"
			sql ++= idxLines.mkString(", \n\t")
		}
		
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
	
	private def fkToSql(fk: ForeignKey, column: Column, targetTableName: String) =
		s"CONSTRAINT ${fk.baseName}_fk FOREIGN KEY ${fk.baseName}_idx (`${
			column.name}`) REFERENCES $targetTableName(id) ON ${if (column.allowsNull) "SET NULL" else "CASCADE"}"
	
	private def indexToSql(index: Index, columnName: String) = s"INDEX ${index.name} (``$columnName`)"
	
	private def columnToSql(column: Column) =
		s"`${column.name}` ${dataTypeToSql(column)}${if (column.allowsNull) "" else " NOT NULL"}"
	
	private def dataTypeToSql(column: Column) = column.dataType match
	{
		case ShortStringType => s"VARCHAR(${if (column.hasIndex) 64 else 255})"
		case IntType => "INT"
		case DoubleType => "DOUBLE"
		case BooleanType => "BOOLEAN"
		case InstantType => "DATETIME" // Timestamp type is already reserved for the 'created' column
	}
	
	private def orderTables(tables: Vector[Table]) =
	{
		var ordered = Vector(tables.head)
		var next = tables.drop(1)
		
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
			// TODO: Handle cases where trying to add tables with circular references
			next = rejectedBuilder.result()
		}
		
		ordered
	}
	
	
	// NESTED	-------------------------
	
	private object TableCreationOrdering extends Ordering[Table]
	{
		override def compare(x: Table, y: Table) =
		{
			if (x.containsReferencesToTableWithId(y.id))
				1
			else if (y.containsReferencesToTableWithId(x.id))
				-1
			else
				x.name.compareTo(y.name)
		}
	}
}
