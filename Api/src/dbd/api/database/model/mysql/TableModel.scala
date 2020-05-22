package dbd.api.database.model.mysql

import dbd.api.database.Tables
import dbd.core.model.existing.mysql
import dbd.core.model.existing.mysql.{Column, Table}
import dbd.core.model.partial.mysql.NewTable
import utopia.flow.datastructure.immutable.{Constant, Model, Value}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.factory.PossiblyMultiLinkedFactory

object TableModel extends PossiblyMultiLinkedFactory[Table, Column]
{
	// IMPLEMENTED	---------------------
	
	override def childFactory = ColumnModel
	
	override def apply(id: Value, model: Model[Constant], children: Seq[Column]) =
	{
		table.requirementDeclaration.validate(model).toTry.map { valid =>
			mysql.Table(id.getInt, valid("releaseId").getInt, valid("classId").getInt, valid("name").getString,
				valid("usesDeprecation").getBoolean, valid("allowsUpdates").getBoolean, children.toVector)
		}
	}
	
	override def table = Tables.table
	
	
	// OTHER	-------------------------
	
	/**
	 * @param releaseId Id of target release
	 * @return A model with only release id set
	 */
	def withReleaseId(releaseId: Int) = apply(releaseId = Some(releaseId))
	
	/**
	 * Inserts a new table to database. Will not include foreign key data
	 * @param releaseId Id of associated release
	 * @param data Table data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted table
	 */
	def insert(releaseId: Int, data: NewTable)(implicit connection: Connection) =
	{
		// Inserts table
		val newId = apply(None, Some(releaseId), Some(data.classId), Some(data.name), Some(data.usesDeprecation),
			Some(data.allowsUpdates)).insert().getInt
		// Inserts all columns
		val newColumns = data.columns.map { c => ColumnModel.insert(newId, c) }
		mysql.Table(newId, releaseId, data.classId, data.name, data.usesDeprecation, data.allowsUpdates, newColumns)
	}
}

/**
 * Used for interacting with table DB data
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class TableModel(id: Option[Int] = None, releaseId: Option[Int], classId: Option[Int] = None, name: Option[String] = None,
					  usesDeprecation: Option[Boolean] = None, allowsUpdates: Option[Boolean] = None)
	extends Storable
{
	override def table = TableModel.table
	
	override def valueProperties = Vector("id" -> id, "releaseId" -> releaseId, "classId" -> classId, "name" -> name,
		"usesDeprecation" -> usesDeprecation, "allowsUpdates" -> allowsUpdates)
}
