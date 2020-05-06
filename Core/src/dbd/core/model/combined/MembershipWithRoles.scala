package dbd.core.model.combined

import dbd.core.model.enumeration.UserRole
import dbd.core.model.existing.Membership
import dbd.core.model.template.Extender

/**
  * Adds role information to a membership
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class MembershipWithRoles(wrapped: Membership, roles: Set[UserRole]) extends Extender[Membership]
