package dbd.api.database.model.mysql

import dbd.api.database.Tables
import dbd.core.model.existing.mysql
import dbd.core.model.existing.mysql.ForeignKey
import dbd.core.model.partial.mysql.NewForeignKey
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.FromValidatedRowModelFactory

object ForeignKeyModel extends FromValidatedRowModelFactory[ForeignKey]
{
	// IMPLEMENTED	---------------------------
	
	override protected def fromValidatedModel(model: Model[Constant]) = mysql.ForeignKey(model("id").getInt,
		model("targetTableId").getInt, model("baseName").getString)
	
	override def table = Tables.foreignKey
	
	
	// OTHER	-------------------------------
	
	/**
	 * Inserts a new foreign key to DB
	 * @param data foreign key data
	 * @param connection DB Connection (implicit)
	 * @return Inserted foreign key
	 */
	def insert(data: NewForeignKey)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.targetTableId), Some(data.baseName)).insert().getInt
		data.withId(newId)
	}
}

/**
 * Used for interacting with foreign key data
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class ForeignKeyModel(id: Option[Int] = None, targetTableId: Option[Int] = None,
						   baseName: Option[String] = None) extends StorableWithFactory[ForeignKey]
{
	override def factory = ForeignKeyModel
	
	override def valueProperties = Vector("id" -> id, "targetTableId" -> targetTableId, "baseName" -> baseName)
}
