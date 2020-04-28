package dbd.client.vc.structure

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.EditSubClassDialog
import dbd.client.model.ParentOrSubClass
import dbd.client.vc.GroupHeader
import dbd.core.util.Log
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.component.Refreshable
import utopia.reflection.component.context.ColorContext
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.shape.LengthExtensions._

/**
 * Used for displaying sub-classes under another class
 * @author Mikko Hilpinen
 * @since 23.1.2020, v0.1
 */
class SubClassesVC(initialParent: ParentOrSubClass, classManager: ClassDisplayManager)(implicit parentContext: ColorContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[ParentOrSubClass]
{
	// ATTRIBUTES	------------------------
	
	import dbd.client.view.DefaultContext._
	
	private implicit val languageCode: String = "en"
	
	private var _parent = initialParent
	private val childStack = Stack.column[ClassVC](margin = margins.small.downscaling)
	private val childManager = ContainerContentManager.forImmutableStates[ParentOrSubClass, ClassVC](childStack) {
		_.classId == _.classId } { c => new ClassVC(c, classManager, parentContext) }
	
	private val view = Stack.buildColumnWithContext(isRelated = true) { stack =>
		stack += parentContext.forTextComponents().use { implicit c => GroupHeader("Sub-Classes") }
		stack += childStack
		// Also adds a new sub-class -button
		parentContext.forTextComponents().forSecondaryColorButtons.use { implicit btnC =>
			stack += ImageAndTextButton.contextual(Icons.addBox.forButtonWithBackground(colorScheme.secondary),
				"Add Sub-Model") {
				parentWindow.foreach { window =>
					val currentParent = content
					new EditSubClassDialog(currentParent.displayedClass.classData, None, classManager).display(window).foreach { _.foreach {
						case Right(newData) =>
							newData match
							{
								case Right(newSubClass) => classManager.addNewSubClass(newSubClass)
								case Left(newLinkConfig) => classManager.addNewLink(newLinkConfig)
							}
						case Left(_) => Log.warning("Somehow received an edit sub-class response from add sub-class dialog")
					} }
				}
			}.alignedToSide(Direction2D.Right, useLowPriorityLength = true)
		}
	}
	
	
	// INITIAL CODE	------------------------
	
	childManager.content = initialParent.subClasses
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: ParentOrSubClass) =
	{
		_parent = newContent
		childManager.content = newContent.subClasses
	}
	
	override def content = _parent
}
