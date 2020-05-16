package dbd.core.model.combined.organization

import dbd.core.model.existing.organization.{Deletion, DeletionCancel}
import dbd.core.model.partial.organization.DeletionData
import dbd.core.model.template.DeepExtender
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.generic.ModelConvertible
import utopia.flow.generic.ValueConversions._

/**
  * Combines organization deletion & possible cancellation data
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
case class DeletionWithCancellations(deletion: Deletion, cancellations: Vector[DeletionCancel])
	extends DeepExtender[Deletion, DeletionData] with ModelConvertible
{
	// COMPUTED	-----------------------------
	
	/**
	  * @return Whether this deletion was cancelled
	  */
	def isCancelled = cancellations.nonEmpty
	
	
	// IMPLEMENTED	-------------------------
	
	override def wrapped = deletion
	
	override def toModel =
	{
		val base = deletion.toModel
		if (isCancelled)
			base + Constant("cancellations", cancellations.map { _.toModel })
		else
			base
	}
}
