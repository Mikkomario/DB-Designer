package dbd.api.database.model

import java.time.Instant

import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.StorableWithFactory

/**
  * Used for interacting with links between devices and their descriptions
  * @author Mikko Hilpinen
  * @since 2.5.2020, v2
  */
trait DescriptionLink[+E, +F <: DescriptionLinkFactory[E, _, _]] extends StorableWithFactory[E]
{
	// ABSTRACT	------------------------------
	
	override def factory: F
	
	/**
	  * @return Description link id (optional)
	  */
	def id: Option[Int]
	
	/**
	  * @return Description target id (optional)
	  */
	def targetId: Option[Int]
	
	/**
	  * @return Description id (optional)
	  */
	def descriptionId: Option[Int]
	
	/**
	  * @return Description deprecation time (optional)
	  */
	def deprecatedAfter: Option[Instant]
	
	
	// IMPLEMENTED	--------------------------
	
	override def valueProperties = Vector("id" -> id, factory.targetIdAttName -> targetId,
		"descriptionId" -> descriptionId, "deprecatedAfter" -> deprecatedAfter)
}
