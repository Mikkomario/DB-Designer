package dbd.core.model.existing

/**
  * Represents a user role registered to DB
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  * @param id Id of this role in the DB
  * @param allowedTaskIds Ids of the tasks that user having this role has access to
  */
case class UserRole(id: Int, allowedTaskIds: Set[Int])