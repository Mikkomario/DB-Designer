package dbd.api.database.model

import java.time.Instant

import dbd.api.database.Tables
import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.nosql.factory.Deprecatable

object Organization extends Deprecatable
{
	// COMPUTED	------------------------------
	
	def table = Tables.organization
	
	// IMPLEMENTED	--------------------------
	
	override val nonDeprecatedCondition = table("deletedAfter").isNull
	
	
	// OTHER	------------------------------
	
	/**
	  * Inserts a new organization to the DB
	  * @param founderId Id of the user who created this organization
	  * @param connection DB Connection (implicit)
	  * @return A new organization
	  */
	def insert(founderId: Int)(implicit connection: Connection) = apply(creatorId = Some(founderId)).insert().getInt
}

/**
  * Used for interacting with organizations in DB
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class Organization(id: Option[Int] = None, creatorId: Option[Int] = None, deletedAfter: Option[Instant] = None) extends Storable
{
	override def table = Organization.table
	
	override def valueProperties = Vector("id" -> id, "deletedAfter" -> deletedAfter, "creatorId" -> creatorId)
}
