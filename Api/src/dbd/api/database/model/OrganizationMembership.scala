package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import dbd.core.model.existing
import dbd.core.model.partial.MembershipData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.nosql.factory.{Deprecatable, StorableFactoryWithValidation}

object OrganizationMembership extends StorableFactoryWithValidation[existing.Membership] with Deprecatable
{
	// IMPLEMENTED	--------------------------
	
	override val nonDeprecatedCondition = table("ended").isNull
	
	override protected def fromValidatedModel(model: Model[Constant]) = existing.Membership(model("id").getInt,
		MembershipData(model("organizationId").getInt, model("userId").getInt, model("creatorId").int,
			model("started").getInstant, model("ended").instant))
	
	override def table = Tables.organizationMembership
	
	
	// OTHER	-------------------------------
	
	/**
	  * Inserts a new membership to DB
	  * @param data Membership data to insert
	  * @param connection DB Connection (implicit)
	  * @return Newly inserted membership
	  */
	def insert(data: MembershipData)(implicit connection: Connection) =
	{
		val newId = apply(None, Some(data.organizationId), Some(data.userId), data.creatorId, Some(data.started), data.ended)
			.insert().getInt
		existing.Membership(newId, data)
	}
}

/**
  * Used for interacting with organization memberships in DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class OrganizationMembership(id: Option[Int] = None, organizationId: Option[Int] = None,
								  userId: Option[Int] = None, creatorId: Option[Int] = None, started: Option[Instant] = None,
								  ended: Option[Instant] = None) extends StorableWithFactory[existing.Membership]
{
	override def factory = OrganizationMembership
	
	override def valueProperties = Vector("id" -> id, "organizationId" -> organizationId, "userId" -> userId,
		"creatorId" -> creatorId, "started" -> started, "ended" -> ended)
}
