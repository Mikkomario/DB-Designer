package dbd.core.model.combined.organization

import dbd.core.model.existing.organization.{Deletion, DeletionCancel}
import dbd.core.model.partial.organization.DeletionData
import dbd.core.model.template.DeepExtender

/**
  * Combines organization deletion & possible cancellation data
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class DeletionWithCancellations(deletion: Deletion, cancellations: Vector[DeletionCancel])
	extends DeepExtender[Deletion, DeletionData]
{
	// COMPUTED	-----------------------------
	
	/**
	  * @return Whether this deletion was cancelled
	  */
	def isCancelled = cancellations.nonEmpty
	
	
	// IMPLEMENTED	-------------------------
	
	override def wrapped = deletion
}
