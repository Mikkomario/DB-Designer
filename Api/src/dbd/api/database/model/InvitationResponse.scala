package dbd.api.database.model

import dbd.api.database.Tables
import dbd.core.model.existing
import dbd.core.model.partial.InvitationResponseData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.StorableFactoryWithValidation

object InvitationResponse extends StorableFactoryWithValidation[existing.InvitationResponse]
{
	// ATTRIBUTES	--------------------------
	
	/**
	  * A model that has been marked as blocked
	  */
	val blocked = apply(wasBlocked = Some(true))
	
	
	// IMPLEMENTED	--------------------------
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.InvitationResponse(model("id").getInt,
		InvitationResponseData(model("invitationId").getInt, model("wasAccepted").getBoolean,
			model("wasBlocked").getBoolean, model("creatorId").getInt))
	
	override def table = Tables.invitationResponse
	
	
	// OTHER	------------------------------
	
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
		existing.InvitationResponse(newId, data)
	}
}

/**
  * Used for interacting with invitation responses in DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class InvitationResponse(id: Option[Int] = None, invitationId: Option[Int] = None,
							  wasAccepted: Option[Boolean] = None, wasBlocked: Option[Boolean] = None,
							  creatorId: Option[Int] = None) extends StorableWithFactory[existing.InvitationResponse]
{
	override def factory = InvitationResponse
	
	override def valueProperties = Vector("id" -> id, "invitationId" -> invitationId, "wasAccepted" -> wasAccepted,
		"wasBlocked" -> wasBlocked, "creatorId" -> creatorId)
}
