package dbd.mysql.database.model

import utopia.flow.util.CollectionExtensions._
import utopia.flow.generic.ValueConversions._
import dbd.mysql.model.existing
import dbd.core.model.enumeration.AttributeType
import dbd.mysql.database.Tables
import dbd.mysql.model.partial.NewColumn
import utopia.vault.database.Connection
import utopia.vault.model.immutable.{Row, StorableWithFactory}
import utopia.vault.model.immutable.factory.FromRowFactory

import scala.util.{Failure, Success}

object Column extends FromRowFactory[existing.Column]
{
	// IMPLEMENTED	----------------------
	
	override def apply(row: Row) =
	{
		table.requirementDeclaration.validate(row(table)).toTry.flatMap { model =>
			// Data type must be parseable
			AttributeType.withId(model("dataType").getInt).flatMap { dataType =>
				// Either attribute or link link must be present
				val attributeLink = ColumnAttributeLink.parseIfPresent(row)
				val linkLink = ColumnLinkLink.parseIfPresent(row)
				
				if (attributeLink.isEmpty && linkLink.isEmpty)
					Failure(new AttributeOrLinkConnectionRequiredException(row))
				else
				{
					val attOrLink = if (attributeLink.isDefined) Right(attributeLink.get) else Left(linkLink.get)
					Success(existing.Column(model("id").getInt, model("tableId").getInt, attOrLink,
						model("name").getString, dataType, model("allowsNull").getBoolean))
				}
			}
		}
	}
	
	override def table = Tables.column
	
	override def joinedTables = ColumnAttributeLink.tables ++ ColumnLinkLink.tables
	
	
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
		// Inserts column first and then links attribute or link connection
		val newId = apply(None, Some(tableId), Some(data.name), Some(data.dataType), Some(data.allowsNull)).insert().getInt
		val newLink = data.linkedData.mapBoth { ColumnLinkLink.insert(newId, _) } { ColumnAttributeLink.insert(newId, _) }
		
		existing.Column(newId, tableId, newLink, data.name, data.dataType, data.allowsNull)
	}
	
	
	// NESTED	-------------------------
	
	private class AttributeOrLinkConnectionRequiredException(row: Row) extends Exception(
		s"Either attribute or link connection is required. Didn't find one from row: $row")
}

/**
 * Used for interacting with column DB data
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Column(id: Option[Int] = None, tableId: Option[Int] = None, name: Option[String] = None,
				  dataType: Option[AttributeType] = None, allowsNull: Option[Boolean] = None)
	extends StorableWithFactory[existing.Column]
{
	override def factory = Column
	
	override def valueProperties = Vector("id" -> id, "tableId" -> tableId, "name" -> name,
		"dataType" -> dataType.map { _.id }, "allowsNull" -> allowsNull)
}
