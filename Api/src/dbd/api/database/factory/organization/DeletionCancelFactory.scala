package dbd.api.database.factory.organization

import dbd.api.database.Tables
import dbd.core.model.existing.organization.DeletionCancel
import dbd.core.model.partial.organization.DeletionCancelData
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import utopia.flow.generic.ValueUnwraps._

/**
  * Used for reading organization deletion cancellations from DB
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
object DeletionCancelFactory extends FromValidatedRowModelFactory[DeletionCancel]
{
	// IMPLEMENTED	-------------------------------
	
	override def table = Tables.organizationDeletionCancellation
	
	override protected def fromValidatedModel(model: Model[Constant]) = DeletionCancel(model("id"),
		DeletionCancelData(model("deletionId"), model("creatorId")))
}
