package dbd.core.model.existing

import dbd.core.model.partial.OrganizationDescriptionData

/**
  * Represents a stored link between an organization and a description
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
case class OrganizationDescription(id: Int, data: OrganizationDescriptionData[Description])
	extends StoredDescriptionLink[OrganizationDescriptionData[Description]]