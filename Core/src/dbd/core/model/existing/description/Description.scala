package dbd.core.model.existing.description

import dbd.core.model.existing.Stored
import dbd.core.model.partial.description.DescriptionData
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Represents a description stored in the database
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
case class Description(id: Int, data: DescriptionData) extends Stored[DescriptionData] with ModelConvertible
{
	override def toModel = data.toModel + Constant("id", id)
}
