package dbd.core.model.existing.organization

import dbd.core.model.existing.Stored
import dbd.core.model.partial.organization.InvitationData
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Represents an organization invitation that has been stored to the DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class Invitation(id: Int, data: InvitationData) extends Stored[InvitationData] with ModelConvertible
{
	// Adds an id to the data model
	override def toModel = data.toModel + Constant("id", id)
}
