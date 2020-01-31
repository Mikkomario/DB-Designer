package dbd.mysql.database.model

import dbd.mysql.database.Tables
import dbd.mysql.model.existing
import dbd.mysql.model.partial.NewForeignKey
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.StorableFactoryWithValidation

object ForeignKey extends StorableFactoryWithValidation[existing.ForeignKey]
{
	// IMPLEMENTED	---------------------------
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.ForeignKey(model("id").getInt,
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
case class ForeignKey(id: Option[Int] = None, targetTableId: Option[Int] = None,
					  baseName: Option[String] = None) extends StorableWithFactory[existing.ForeignKey]
{
	override def factory = ForeignKey
	
	override def valueProperties = Vector("id" -> id, "targetTableId" -> targetTableId, "baseName" -> baseName)
}
