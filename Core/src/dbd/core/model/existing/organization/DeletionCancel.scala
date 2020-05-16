package dbd.core.model.existing.organization

import dbd.core.model.existing.Stored
import dbd.core.model.partial.organization.DeletionCancelData

/**
  * Stored organization deletion cancellation
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class DeletionCancel(id: Int, data: DeletionCancelData) extends Stored[DeletionCancelData]
