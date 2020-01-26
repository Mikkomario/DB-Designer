package dbd.client.vc

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.EditSubClassDialog
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.{Fonts, ParentOrSubClass}
import dbd.core.util.Log
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Used for displaying sub-classes under another class
 * @author Mikko Hilpinen
 * @since 23.1.2020, v0.1
 */
class SubClassesVC(initialParent: ParentOrSubClass, classManager: ClassDisplayManager)
				  (implicit baseCB: ComponentContextBuilder, margins: Margins, colorScheme: ColorScheme, fonts: Fonts,
				   defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[ParentOrSubClass]
{
	// ATTRIBUTES	------------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private var _parent = initialParent
	private val childStack = Stack.row[ClassVC](margin = margins.small.downscaling)
	private val childManager = new ContainerContentManager[ParentOrSubClass, Stack[ClassVC], ClassVC](childStack)(
		c => new ClassVC(c, classManager))
	
	private val view = Stack.buildColumnWithContext(isRelated = true) { stack =>
		stack += GroupHeader("Sub-Classes")
		stack += childStack
		// Also adds a new sub-class -button
		stack += ImageAndTextButton.contextual(Icons.addBox.forButtonWithBackground(colorScheme.secondary),
			"Add Sub-Model") { () =>
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
		} (baseCB.withColors(colorScheme.secondary).result).alignedToSide(Direction2D.Right, useLowPriorityLength = true)
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
