package dbd.core.model.existing.organization

import dbd.core.model.existing.Stored
import dbd.core.model.partial.organization.InvitationResponseData
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Represents an invitation response that has been stored to DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class InvitationResponse(id: Int, data: InvitationResponseData) extends Stored[InvitationResponseData]
	with ModelConvertible
{
	override def toModel =
	{
		// Includes invitation id
		val base = data.toModel
		base + Constant("id", id)
	}
}
