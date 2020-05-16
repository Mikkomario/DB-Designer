package dbd.api.database.model.organization

/**
  * Used for interacting with organization deletion cancellation data
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  */
// TODO: Continue
case class OrganizationDeletionCancelModel(id: Option[Int] = None, deletionId: Option[Int] = None, creatorId: Option[Int] = None)
