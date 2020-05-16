package dbd.client.model

import dbd.core.model.existing.database
import dbd.core.model.existing.database.Attribute

object ChangedItems
{
	/**
	  * An empty changes list
	  */
	val empty = ChangedItems(Vector(), Map(), Vector())
}

/**
  * A model that lists items that are changed in some way
  * @author Mikko Hilpinen
  * @since 2.2.2020, v0.1
  * @param classes Affected classes
  * @param attributes Affected attributes (in cases where classes were not updated), grouped by class
  * @param links Affected links between classes, by class pairs
  */
case class ChangedItems(classes: Vector[database.Class], attributes: Map[database.Class, Attribute], links: Vector[(database.Class, database.Class)])
{
	/**
	  * @return Whether this list of changes is empty
	  */
	def isEmpty = classes.isEmpty && attributes.isEmpty && links.isEmpty
	
	/**
	  * @return Whether this list of changes is not empty
	  */
	def nonEmpty = !isEmpty
}