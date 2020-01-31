package dbd.mysql.database.model

import dbd.mysql.database.Tables
import utopia.flow.generic.ValueConversions._
import dbd.mysql.model.existing
import dbd.mysql.model.partial.NewIndex
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.StorableFactoryWithValidation

object Index extends StorableFactoryWithValidation[existing.Index]
{
	// IMPLEMENTED	-----------------------
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.Index(model("id").getInt,
		model("name").getString)
	
	override def table = Tables.index
	
	
	// OTHER	---------------------------
	
	/**
	 * Inserts a new index to database
	 * @param data Index data
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted index
	 */
	def insert(data: NewIndex)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.name)).insert().getInt
		existing.Index(newId, data.name)
	}
}

/**
 * Used for interacting with index DB data
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class Index(id: Option[Int] = None, name: Option[String] = None)
	extends StorableWithFactory[existing.Index]
{
	override def factory = Index
	
	override def valueProperties = Vector("id" -> id, "name" -> name)
}
