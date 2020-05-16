package dbd.api.database.model.organization

import dbd.api.database.Tables
import dbd.core.model.existing.organization
import dbd.core.model.partial.organization.InvitationResponseData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.FromValidatedRowModelFactory

object InvitationResponseModel extends FromValidatedRowModelFactory[organization.InvitationResponse]
{
	// ATTRIBUTES	--------------------------
	
	/**
	  * A model that has been marked as blocked
	  */
	val blocked = apply(wasBlocked = Some(true))
	
	
	// IMPLEMENTED	--------------------------
	
	override protected def fromValidatedModel(model: Model[Constant]) = organization.InvitationResponse(model("id").getInt,
		InvitationResponseData(model("invitationId").getInt, model("wasAccepted").getBoolean,
			model("wasBlocked").getBoolean, model("creatorId").getInt))
	
	override def table = Tables.invitationResponse
	
	
	// OTHER	------------------------------
	
	/**
	  * @param invitationId Id of the invitation this response is for
	  * @return A model with only invitation id set
	  */
	def withInvitationId(invitationId: Int) = apply(invitationId = Some(invitationId))
	
	/**
	  * Inserts a new invitation response to the DB
	  * @param data Data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted response
	  */
	def insert(data: InvitationResponseData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.invitationId), Some(data.wasAccepted), Some(data.wasBlocked),
			Some(data.creatorId)).insert().getInt
		organization.InvitationResponse(newId, data)
	}
}

/**
  * Used for interacting with invitation responses in DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class InvitationResponseModel(id: Option[Int] = None, invitationId: Option[Int] = None,
								   wasAccepted: Option[Boolean] = None, wasBlocked: Option[Boolean] = None,
								   creatorId: Option[Int] = None) extends StorableWithFactory[organization.InvitationResponse]
{
	override def factory = InvitationResponseModel
	
	override def valueProperties = Vector("id" -> id, "invitationId" -> invitationId, "wasAccepted" -> wasAccepted,
		"wasBlocked" -> wasBlocked, "creatorId" -> creatorId)
}
