package dbd.client.vc

import dbd.core.model.existing.Database
import utopia.reflection.component.input.SelectableWithPointers
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper

/**
  * Used for selecting currently modified database and editing database configurations
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class DatabaseSelectionVC extends StackableAwtComponentWrapperWrapper
	with SelectableWithPointers[Database, Vector[Database]]
{
	override protected def wrapped = ???
	
	override def contentPointer = ???
	
	override def valuePointer = ???
}
