package dbd.client.vc

import dbd.client.model.DisplayedRelease
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper

/**
  * Displays data of a single release (published or upcoming)
  * @author Mikko Hilpinen
  * @since 2.2.2020, v0.1
  */
class ReleaseVC(initialRelease: DisplayedRelease) extends StackableAwtComponentWrapperWrapper with Refreshable[DisplayedRelease]
{
	// ATTRIBUTES	------------------------
	
	private val _content = initialRelease
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = ???
	
	override def content_=(newContent: DisplayedRelease) = ???
	
	override def content = ???
}
