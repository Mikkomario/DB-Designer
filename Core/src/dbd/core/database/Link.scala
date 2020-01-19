package dbd.core.database

import utopia.flow.generic.ValueConversions._
import dbd.core.model.existing
import utopia.vault.model.immutable.access.{ConditionalSingleAccess, ItemAccess, NonDeprecatedSingleAccess}

/**
 * Used for accessing individual links
 * @author Mikko Hilpinen
 * @since 19.1.2020, v0.1
 */
object Link extends NonDeprecatedSingleAccess[existing.Link]
{
	// IMPLEMENTED	------------------------
	
	override def factory = model.Link
	
	
	// NESTED	----------------------------
	
	class LinkById(linkId: Int) extends ItemAccess[existing.Link](linkId, Link.factory)
	{
		object Configuration extends ConditionalSingleAccess[existing.LinkConfiguration]
		{
			// IMPLEMENTED	----------------
			
			override def condition = factory.withLinkId(linkId).toCondition && factory.nonDeprecatedCondition
			
			override def factory = model.LinkConfiguration
			
			
			// OTHER	--------------------
			
			// TODO: Add update, delete etc. methods
		}
	}
}
