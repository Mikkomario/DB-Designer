package dbd.core.model.enumeration

/**
  * A common trait for description role values
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
sealed trait DescriptionRole
{
	/**
	  * @return Id of this role
	  */
	def id: Int
}

object DescriptionRole
{
	/**
	  * Name descriptions simply name various resources
	  */
	case object Name extends DescriptionRole
	{
		override def id = 1
	}
	
	/**
	  * All description role values recorded in code
	  */
	val values = Vector[DescriptionRole](Name)
	
	/**
	  * @param id Description role id
	  * @return a role matching specified id
	  */
	def forId(id: Int) = values.find { _.id == id }
}
