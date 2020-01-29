package dbd.mysql.database.model

import dbd.mysql.database.Tables
import utopia.flow.generic.ValueConversions._
import dbd.mysql.model.existing
import dbd.mysql.model.partial.NewColumnAttributeLink
import utopia.vault.database.Connection
import utopia.vault.model.immutable.{Row, StorableWithFactory}
import utopia.vault.model.immutable.factory.FromRowFactory

object ColumnAttributeLink extends FromRowFactory[existing.ColumnAttributeLink]
{
	// IMPLEMENTED	-----------------------
	
	override def apply(row: Row) =
	{
		table.requirementDeclaration.validate(row(table)).toTry.map { model =>
			val index = Index.parseIfPresent(row)
			existing.ColumnAttributeLink(model("id").getInt, model("columnId").getInt,
				model("attributeConfigurationId").getInt, index)
		}
	}
	
	override def table = Tables.columnAttributeLink
	
	override def joinedTables = Index.tables
	
	
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
		val newIndex = data.index.map { Index.insert(_) }
		val newId = apply(None, Some(columnId), Some(data.attributeConfigurationId), newIndex.map { _.id }).insert().getInt
		existing.ColumnAttributeLink(newId, columnId, data.attributeConfigurationId, newIndex)
	}
}

/**
 * Used for interacting with column-attribute-links in DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class ColumnAttributeLink(id: Option[Int] = None, columnId: Option[Int] = None, attributeConfigurationId: Option[Int] = None,
							   indexId: Option[Int] = None)
	extends StorableWithFactory[existing.ColumnAttributeLink]
{
	override def factory = ColumnAttributeLink
	
	override def valueProperties = Vector("id" -> id, "columnId" -> columnId,
		"attributeConfigurationId" -> attributeConfigurationId, "indexId" -> indexId)
}
