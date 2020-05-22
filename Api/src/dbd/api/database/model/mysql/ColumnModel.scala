package dbd.api.database.model.mysql

import dbd.api.database.Tables
import dbd.core.model.enumeration.AttributeType
import dbd.core.model.existing.mysql
import dbd.core.model.existing.mysql.Column
import dbd.core.model.partial.mysql.NewColumn
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.CollectionExtensions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.{Row, StorableWithFactory}
import utopia.vault.nosql.factory.FromRowFactory
import utopia.vault.sql.JoinType

import scala.util.{Failure, Success}

object ColumnModel extends FromRowFactory[Column]
{
	// IMPLEMENTED	----------------------
	
	override def joinType = JoinType.Left
	
	override def apply(row: Row) =
	{
		table.requirementDeclaration.validate(row(table)).toTry.flatMap { model =>
			// Data type must be parseable
			AttributeType.withId(model("dataType").getInt).flatMap { dataType =>
				// Either attribute or link link must be present
				val attributeLink = ColumnAttributeLinkModel.parseIfPresent(row)
				val linkLink = ColumnLinkLinkModel.parseIfPresent(row)
				
				if (attributeLink.isEmpty && linkLink.isEmpty)
					Failure(new AttributeOrLinkConnectionRequiredException(row))
				else
				{
					val attOrLink = if (attributeLink.isDefined) Right(attributeLink.get) else Left(linkLink.get)
					Success(mysql.Column(model("id").getInt, model("tableId").getInt, attOrLink,
						model("name").getString, dataType, model("allowsNull").getBoolean))
				}
			}
		}
	}
	
	override def table = Tables.column
	
	override def joinedTables = ColumnAttributeLinkModel.tables ++ ColumnLinkLinkModel.tables
	
	
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
		val newLink = data.linkedData.mapBoth { ColumnLinkLinkModel.insert(newId, _) } { ColumnAttributeLinkModel.insert(newId, _) }
		
		mysql.Column(newId, tableId, newLink, data.name, data.dataType, data.allowsNull)
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
case class ColumnModel(id: Option[Int] = None, tableId: Option[Int] = None, name: Option[String] = None,
					   dataType: Option[AttributeType] = None, allowsNull: Option[Boolean] = None)
	extends StorableWithFactory[Column]
{
	override def factory = ColumnModel
	
	override def valueProperties = Vector("id" -> id, "tableId" -> tableId, "name" -> name,
		"dataType" -> dataType.map { _.id }, "allowsNull" -> allowsNull)
}
