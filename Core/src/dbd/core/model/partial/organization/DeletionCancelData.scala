package dbd.core.model.partial.organization

/**
  * Contains basic information about an organization deletion cancellation
  * @author Mikko Hilpinen
  * @since 16.5.2020, v2
  * @param deletionId Id of the cancelled deletion
  * @param creatorId Id of the user who cancelled the deletion (if known)
  */
case class DeletionCancelData(deletionId: Int, creatorId: Option[Int] = None)
