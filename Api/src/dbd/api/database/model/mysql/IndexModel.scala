package dbd.api.database.model.mysql

import dbd.api.database.Tables
import dbd.core.model.existing.mysql
import dbd.core.model.existing.mysql.Index
import dbd.core.model.partial.mysql.NewIndex
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.FromValidatedRowModelFactory

object IndexModel extends FromValidatedRowModelFactory[Index]
{
	// IMPLEMENTED	-----------------------
	
	override protected def fromValidatedModel(model: Model[Constant]) = mysql.Index(model("id").getInt,
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
		mysql.Index(newId, data.name)
	}
}

/**
 * Used for interacting with index DB data
 * @author Mikko Hilpinen
 * @since 28.1.2020, v0.1
 */
case class IndexModel(id: Option[Int] = None, name: Option[String] = None)
	extends StorableWithFactory[Index]
{
	override def factory = IndexModel
	
	override def valueProperties = Vector("id" -> id, "name" -> name)
}
