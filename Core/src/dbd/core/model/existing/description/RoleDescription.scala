package dbd.core.model.existing.description

import dbd.core.model.partial.description.RoleDescriptionData

/**
  * Represents a stored role-description-link
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
@deprecated("Replaced with DescriptionLink", "v2")
case class RoleDescription(id: Int, data: RoleDescriptionData[Description])
	extends StoredDescriptionLink[RoleDescriptionData[Description]]
