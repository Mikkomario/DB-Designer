package dbd.api.database.model.mysql

import dbd.api.database.Tables
import dbd.api.database.model.database.LinkConfigurationModel
import dbd.core.model.existing.mysql
import dbd.core.model.existing.mysql.ColumnLinkLink
import dbd.core.model.partial.mysql.NewColumnLinkLink
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.{Row, StorableWithFactory}
import utopia.vault.nosql.factory.FromRowFactory
import utopia.vault.sql.JoinType

object ColumnLinkLinkModel extends FromRowFactory[ColumnLinkLink]
{
	// IMPLEMENTED	--------------------------
	
	override def apply(row: Row) = table.requirementDeclaration.validate(row(table)).toTry.flatMap { valid =>
		ForeignKeyModel(row).flatMap { foreignKey =>
			LinkConfigurationModel(row).map { linkConfig =>
				mysql.ColumnLinkLink(valid("id").getInt, valid("columnId").getInt, linkConfig, foreignKey)
			}
		}
	}
	
	override def joinType = JoinType.Inner
	
	override def joinedTables = ForeignKeyModel.tables ++ LinkConfigurationModel.tables
	
	override def table = Tables.columnLinkLink
	
	
	// OTHER	-------------------------------
	
	/**
	 * Inserts a new column-link-link to DB
	 * @param columnId Id of linked column
	 * @param data Link data
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted link
	 */
	def insert(columnId: Int, data: NewColumnLinkLink)(implicit connection: Connection) =
	{
		// Inserts the foreign key first
		val newFK = ForeignKeyModel.insert(data.foreignKey)
		val newId = apply(None, Some(columnId), Some(data.linkConfiguration.id), Some(newFK.id)).insert().getInt
		ColumnLinkLink(newId, columnId, data.linkConfiguration, newFK)
	}
}

/**
 * Used for interacting with column-link-links in DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class ColumnLinkLinkModel(id: Option[Int] = None, columnId: Option[Int] = None, linkConfigurationId: Option[Int] = None,
							   foreignKeyId: Option[Int] = None) extends StorableWithFactory[ColumnLinkLink]
{
	override def factory = ColumnLinkLinkModel
	
	override def valueProperties = Vector("id" -> id, "columnId" -> columnId, "linkConfigurationId" -> linkConfigurationId,
		"foreignKeyId" -> foreignKeyId)
}
