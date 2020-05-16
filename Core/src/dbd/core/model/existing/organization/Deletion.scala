package dbd.core.model.existing.organization

import dbd.core.model.existing.StoredModelConvertible
import dbd.core.model.partial.organization.DeletionData

/**
  * Represents a stored organization deletion attempt
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class Deletion(id: Int, data: DeletionData) extends StoredModelConvertible[DeletionData]
