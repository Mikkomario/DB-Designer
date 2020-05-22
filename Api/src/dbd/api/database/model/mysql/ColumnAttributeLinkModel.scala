package dbd.api.database.model.mysql

import dbd.api.database.Tables
import dbd.api.database.model.database.AttributeConfigurationModel
import dbd.core.model.existing.mysql
import dbd.core.model.existing.mysql.ColumnAttributeLink
import dbd.core.model.partial.mysql.NewColumnAttributeLink
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.{Row, StorableWithFactory}
import utopia.vault.nosql.factory.FromRowFactory
import utopia.vault.sql.JoinType

object ColumnAttributeLinkModel extends FromRowFactory[ColumnAttributeLink]
{
	// IMPLEMENTED	-----------------------
	
	override def apply(row: Row) =
	{
		table.requirementDeclaration.validate(row(table)).toTry.flatMap { model =>
			// Attribute configuration must be parseable
			AttributeConfigurationModel(row).map { attributeConfig =>
				// index is optional
				val index = IndexModel.parseIfPresent(row)
				mysql.ColumnAttributeLink(model("id").getInt, model("columnId").getInt,
					attributeConfig, index)
			}
		}
	}
	
	override def joinType = JoinType.Left
	
	override def table = Tables.columnAttributeLink
	
	override def joinedTables = IndexModel.tables ++ AttributeConfigurationModel.tables
	
	
	// OTHER	-------------------------
	
	/**
	 * Inserts a new column-attribute-link to DB
	 * @param columnId Id of linked column
	 * @param data Link data
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted link
	 */
	def insert(columnId: Int, data: NewColumnAttributeLink)(implicit connection: Connection) =
	{
		val newIndex = data.index.map { IndexModel.insert(_) }
		val newId = apply(None, Some(columnId), Some(data.attributeConfiguration.id), newIndex.map { _.id }).insert().getInt
		mysql.ColumnAttributeLink(newId, columnId, data.attributeConfiguration, newIndex)
	}
}

/**
 * Used for interacting with column-attribute-links in DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class ColumnAttributeLinkModel(id: Option[Int] = None, columnId: Option[Int] = None, attributeConfigurationId: Option[Int] = None,
									indexId: Option[Int] = None)
	extends StorableWithFactory[ColumnAttributeLink]
{
	override def factory = ColumnAttributeLinkModel
	
	override def valueProperties = Vector("id" -> id, "columnId" -> columnId,
		"attributeConfigurationId" -> attributeConfigurationId, "indexId" -> indexId)
}
