package dbd.client.model

import dbd.core.model.partial.database.{NewClassInfo, NewLinkConfiguration}

/**
 * Contains resulting data on sub-class edit
 * @author Mikko Hilpinen
 * @since 26.1.2020, v0.1
 * @param linkModification New configuration for the link between classes. None if there was no change in link configuration.
 * @param classModification New class information for the sub-class. None if class information didn't change
 */
case class EditSubClassResult(linkModification: Option[NewLinkConfiguration] = None,
							  classModification: Option[NewClassInfo] = None)
