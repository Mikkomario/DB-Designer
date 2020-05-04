package dbd.core.model.enumeration

import dbd.core.model.error.NoSuchTypeException
import utopia.flow.util.CollectionExtensions._

/**
  * An enumeration for user roles within an organization
  * @author Mikko Hilpinen
  * @since 4.5.2020, v2
  */
sealed trait UserRole
{
	/**
	  * @return Row id of this role in the DB
	  */
	def id: Int
}

object UserRole
{
	/**
	  * Owners have read/write access to all data in an organization
	  */
	case object Owner extends UserRole
	{
		override val id = 1
	}
	
	/**
	  * All registered user roles
	  */
	val values = Vector[UserRole](Owner)
	
	/**
	  * @param id Role id
	  * @return A role matching specified id. Failure if no such role was found.
	  */
	def forId(id: Int) = values.find { _.id == id }.toTry { new NoSuchTypeException(
		s"There doesn't exist any user role with id $id") }
}
