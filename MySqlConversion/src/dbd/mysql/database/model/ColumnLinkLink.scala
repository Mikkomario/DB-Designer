package dbd.mysql.database.model

import dbd.mysql.database.Tables
import utopia.flow.generic.ValueConversions._
import dbd.mysql.model.existing
import dbd.mysql.model.partial.NewColumnLinkLink
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.LinkedStorableFactory

object ColumnLinkLink extends LinkedStorableFactory[existing.ColumnLinkLink, existing.ForeignKey]
{
	// IMPLEMENTED	--------------------------
	
	override def childFactory = ForeignKey
	
	override def apply(model: Model[Constant], child: existing.ForeignKey) = table.requirementDeclaration
		.validate(model).toTry.map { valid =>
		existing.ColumnLinkLink(valid("id").getInt, valid("columnId").getInt, valid("linkConfigurationId").getInt, child)
	}
	
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
		val newFK = ForeignKey.insert(data.foreignKey)
		val newId = apply(None, Some(columnId), Some(data.linkConfigurationId), Some(newFK.id)).insert().getInt
		existing.ColumnLinkLink(newId, columnId, data.linkConfigurationId, newFK)
	}
}

/**
 * Used for interacting with column-link-links in DB
 * @author Mikko Hilpinen
 * @since 29.1.2020, v0.1
 */
case class ColumnLinkLink(id: Option[Int] = None, columnId: Option[Int] = None, linkConfigurationId: Option[Int] = None,
						  foreignKeyId: Option[Int] = None) extends StorableWithFactory[existing.ColumnLinkLink]
{
	override def factory = ColumnLinkLink
	
	override def valueProperties = Vector("id" -> id, "columnId" -> columnId, "linkConfigurationId" -> linkConfigurationId,
		"foreignKeyId" -> foreignKeyId)
}
