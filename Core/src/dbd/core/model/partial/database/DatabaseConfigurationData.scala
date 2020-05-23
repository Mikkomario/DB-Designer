package dbd.core.model.partial.database

import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Contains basic data about a single database configuration
  * @author Mikko Hilpinen
  * @since 23.5.2020, v2
  */
case class DatabaseConfigurationData(name: String, creatorId: Option[Int] = None) extends ModelConvertible
{
	override def toModel = Model(Vector("name" -> name, "creator_id" -> creatorId))
}
