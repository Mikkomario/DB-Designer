package dbd.core.model.combined

import dbd.core.model.existing.OrganizationDescription

/**
  * Contains more information about an organization from a single member's perspective
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  * @param id Organization id
  * @param userId Id of the described user
  * @param descriptions Various descriptions of this organization
  * @param myRoles Described user's roles in this organization
  */
case class MyOrganization(id: Int, userId: Int, descriptions: Set[OrganizationDescription], myRoles: Set[DescribedRole])
