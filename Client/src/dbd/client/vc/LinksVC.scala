package dbd.client.vc

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.EditLinkDialog
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.{DisplayedClass, DisplayedLink}
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
 * Displays a number of links
 * @author Mikko Hilpinen
 * @since 20.1.2020, v0.1
 */
class LinksVC(initialClass: DisplayedClass, classManager: ClassDisplayManager)
			 (implicit margins: Margins, baseCB: ComponentContextBuilder, colorScheme: ColorScheme,
			  localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[DisplayedClass]
{
	// ATTRIBUTES	------------------------
	
	private implicit val language: String = "en"
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private var _content = initialClass
	
	private val buttonsStack = Stack.column[LinkRowVC](margins.small.downscaling)
	private val manager = new ContainerContentManager[(Int, DisplayedLink), Stack[LinkRowVC], LinkRowVC](buttonsStack)({
		case (id, link) => new LinkRowVC(id, link, classManager) })
	
	private val view = Stack.buildColumnWithContext(isRelated = true) { mainStack =>
		mainStack += GroupHeader("Links")
		mainStack += buttonsStack
		val addButtonColor = colorScheme.secondary
		// When add link button is pressed, displays a dialog and inserts newly created link to DB and displayed data
		mainStack += ImageAndTextButton.contextual(Icons.addBox.forButtonWithBackground(addButtonColor), "Add Link") { () =>
			parentWindow.foreach { window =>
				val classToEdit = _content
				new EditLinkDialog(None, classToEdit.classData, classManager.linkableClasses(classToEdit.classId))
					.display(window).foreach { _.foreach { newLink => classManager.addNewLink(newLink)
			} } }
		}.alignedToSide(Direction2D.Right, useLowPriorityLength = true)
	}
	
	
	// INITIAL CODE	------------------------
	
	manager.content = initialClass.links.map { initialClass.classId -> _ }
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: DisplayedClass) =
	{
		_content = newContent
		manager.content = newContent.links.map { newContent.classId -> _ }
	}
	
	override def content = _content
}
