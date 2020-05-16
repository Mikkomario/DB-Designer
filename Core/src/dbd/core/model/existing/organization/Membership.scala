package dbd.core.model.existing.organization

import dbd.core.model.existing.Stored
import dbd.core.model.partial.organization.MembershipData

/**
  * Represents a stored organization membership
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class Membership(id: Int, data: MembershipData) extends Stored[MembershipData]
