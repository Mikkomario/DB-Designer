package dbd.core.model.existing

import dbd.core.model.partial.RoleDescriptionData

/**
  * Represents a stored role-description-link
  * @author Mikko Hilpinen
  * @since 6.5.2020, v2
  */
case class RoleDescription(id: Int, data: RoleDescriptionData[Description])
	extends StoredDescriptionLink[RoleDescriptionData[Description]]
