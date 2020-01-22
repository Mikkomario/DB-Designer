package dbd.client.vc

import dbd.client.controller.ClassDisplayManager
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.{DisplayedClass, Fonts}
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
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
class SubClassesVC(initialParent: DisplayedClass, classManager: ClassDisplayManager)
				  (implicit baseCB: ComponentContextBuilder, margins: Margins, colorScheme: ColorScheme, fonts: Fonts,
				   defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[DisplayedClass]
{
	// ATTRIBUTES	------------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private var _parent = initialParent
	private val childStack = Stack.row[ClassVC](margin = margins.small.downscaling)
	private val childManager = new ContainerContentManager[DisplayedClass, Stack[ClassVC], ClassVC](childStack)(
		c => new ClassVC(c, classManager))
	
	private val view = Stack.buildColumnWithContext(isRelated = true) { stack =>
		stack += GroupHeader("Sub-Classes")
		stack += childStack
	}
	
	
	// INITIAL CODE	------------------------
	
	childManager.content = initialParent.childLinks.map { _.child }
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: DisplayedClass) =
	{
		_parent = newContent
		childManager.content = newContent.childLinks.map { _.child }
	}
	
	override def content = _parent
}
