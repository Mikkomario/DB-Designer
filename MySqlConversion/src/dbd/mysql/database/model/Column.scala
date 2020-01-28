package dbd.mysql.database.model

import utopia.flow.generic.ValueConversions._
import dbd.mysql.model.existing
import dbd.core.model.enumeration.AttributeType
import dbd.mysql.database.Tables
import dbd.mysql.model.partial.NewColumn
import utopia.vault.database.Connection
import utopia.vault.model.immutable.{Row, StorableWithFactory}
import utopia.vault.model.immutable.factory.FromRowFactory
import utopia.vault.util.ErrorHandling

import scala.util.{Failure, Success}

object Column extends FromRowFactory[existing.Column]
{
	// IMPLEMENTED	----------------------
	
	override def apply(row: Row) =
	{
		table.requirementDeclaration.validate(row(table)).toTry.flatMap { model =>
			// Data type must be parseable
			AttributeType.withId(model("dataType").getInt).map { dataType =>
				// Parses index and foreign key data, if present
				val index = if (row.containsDataForTable(Index.table)) Index(row) else None
				val fk = if (row.containsDataForTable(ForeignKey.table)) ForeignKey(row) else None
				
				existing.Column(model("id").getInt, model("tableId").getInt, model("attributeId").getInt,
					model("name").getString, dataType, model("allowsNull").getBoolean, index, fk)
			}
		} match
		{
			case Success(column) => Some(column)
			case Failure(error) => ErrorHandling.modelParsePrinciple.handle(error); None
		}
	}
	
	override def table = Tables.column
	
	override def joinedTables = Index.tables ++ ForeignKey.tables
	
	
	// OTHER	------------------------
	
	/**
	 * Inserts a new column to database. Will not include foreign key data at this point.
	 * @param tableId Id of targeted table
	 * @param data New column data
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted column
	 */
	def insert(tableId: Int, data: NewColumn)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(tableId), Some(data.attributeId), Some(data.name), Some(data.dataType),
			Some(data.allowsNull)).insert().getInt
		val newIndex = data.index.map { Index.insert(newId, _) }
		existing.Column(newId, tableId, data.attributeId, data.name, data.dataType, data.allowsNull, newIndex, None)
	}
}

/**
 * Used for interacting with column DB data
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Column(id: Option[Int] = None, tableId: Option[Int] = None, attributeId: Option[Int] = None,
				  name: Option[String] = None, dataType: Option[AttributeType] = None,
				  allowsNull: Option[Boolean] = None) extends StorableWithFactory[existing.Column]
{
	override def factory = Column
	
	override def valueProperties = Vector("id" -> id, "tableId" -> tableId, "attributeId" -> attributeId, "name" -> name,
		"dataType" -> dataType.map { _.id }, "allowsNull" -> allowsNull)
}
