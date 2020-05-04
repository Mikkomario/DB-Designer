package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.existing
import dbd.core.model.enumeration.UserRole
import dbd.core.model.error.NoDataFoundException
import dbd.core.model.partial.InvitationData
import utopia.flow.datastructure.template.{Model, Property}
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.CollectionExtensions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.StorableFactory

import scala.util.{Failure, Success}

object Invitation extends StorableFactory[existing.Invitation]
{
	// IMPLEMENTED	---------------------------
	
	override def table = Tables.organizationInvitation
	
	override def apply(model: Model[Property]) = table.requirementDeclaration.validate(model).toTry.flatMap { valid =>
		// Role must be parseable
		UserRole.forId(valid("startingRoleId").getInt).flatMap { role =>
			// Either recipient id or recipient email must be provided
			val recipient =
			{
				valid("recipientId").int.map { id => Success(Right(id)) }.orElse { valid("recipientEmail").string.map {
					email => Success(Left(email)) } }.getOrElse(Failure(new NoDataFoundException(
					s"Didn't find recipientId or recipientEmail from $valid")))
			}
			recipient.map { recipient =>
				existing.Invitation(valid("id").getInt, InvitationData(valid("organizationId").getInt, recipient,
					role, valid("expiresIn").getInstant, valid("creatorId").int))
			}
		}
	}
	
	
	// OTHER	--------------------------------
	
	/**
	  * Inserts a new invitation to the database
	  * @param data Invitation data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted invitation
	  */
	def insert(data: InvitationData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.organizationId), data.recipient.rightOption, data.recipient.leftOption,
			Some(data.startingRole), Some(data.expireTime), data.creatorId).insert().getInt
		existing.Invitation(newId, data)
	}
}

/**
  * Used for interacting with organization invitations in DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class Invitation(id: Option[Int] = None, organizationId: Option[Int] = None, recipientId: Option[Int] = None,
					  recipientEmail: Option[String] = None, startingRole: Option[UserRole] = None,
					  expireTime: Option[Instant] = None, creatorId: Option[Int] = None)
	extends StorableWithFactory[existing.Invitation]
{
	override def factory = Invitation
	
	override def valueProperties = Vector("id" -> id, "organizationId" -> organizationId, "recipientId" -> recipientId,
		"recipientEmail" -> recipientEmail, "startingRoleId" -> startingRole.map { _.id }, "expiresIn" -> expireTime,
		"creatorId" -> creatorId)
}
