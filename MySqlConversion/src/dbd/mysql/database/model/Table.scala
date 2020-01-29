package dbd.mysql.database.model

import dbd.mysql.database.Tables
import utopia.flow.generic.ValueConversions._
import dbd.mysql.model.existing
import dbd.mysql.model.partial.NewTable
import utopia.flow.datastructure.immutable.{Constant, Model, Value}
import utopia.vault.database.Connection
import utopia.vault.model.immutable.factory.MultiLinkedStorableFactory
import utopia.vault.model.immutable.Storable

object Table extends MultiLinkedStorableFactory[existing.Table, existing.Column]
{
	// IMPLEMENTED	---------------------
	
	override def childFactory = Column
	
	override def apply(id: Value, model: Model[Constant], children: Seq[existing.Column]) =
	{
		table.requirementDeclaration.validate(model).toTry.map { valid =>
			existing.Table(id.getInt, valid("releaseId").getInt, valid("classId").getInt, valid("name").getString,
				valid("usesDeprecation").getBoolean, valid("allowsUpdates").getBoolean, children.toVector)
		}
	}
	
	override def table = Tables.table
	
	
	// OTHER	-------------------------
	
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
		val newColumns = data.columns.map { c => Column.insert(newId, c) }
		existing.Table(newId, releaseId, data.classId, data.name, data.usesDeprecation, data.allowsUpdates, newColumns)
	}
}

/**
 * Used for interacting with table DB data
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Table(id: Option[Int] = None, releaseId: Option[Int], classId: Option[Int], name: Option[String] = None,
				 usesDeprecation: Option[Boolean] = None, allowsUpdates: Option[Boolean] = None)
	extends Storable
{
	override def table = Table.table
	
	override def valueProperties = Vector("id" -> id, "releaseId" -> releaseId, "classId" -> classId, "name" -> name,
		"usesDeprecation" -> usesDeprecation, "allowsUpdates" -> allowsUpdates)
}
