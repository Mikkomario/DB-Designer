package dbd.client.vc.structure

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.EditLinkDialog
import dbd.client.model.{DisplayedClass, DisplayedLink}
import dbd.client.vc.GroupHeader
import dbd.core.model.existing.Class
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.component.Refreshable
import utopia.reflection.component.context.ColorContext
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.Stack
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.shape.Alignment.Center
import utopia.reflection.shape.LengthExtensions._

/**
 * Displays a number of links
 * @author Mikko Hilpinen
 * @since 20.1.2020, v0.1
 */
class LinksVC(initialClass: DisplayedClass, classManager: ClassDisplayManager)(implicit context: ColorContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[DisplayedClass]
{
	// ATTRIBUTES	------------------------
	
	import dbd.client.view.DefaultContext._
	
	private implicit val language: String = "en"
	
	private var _content = initialClass
	
	private val buttonsStack = Stack.column[LinkRowVC](margins.small.downscaling)
	private val manager = ContainerContentManager.forImmutableStates[(Class, DisplayedLink), LinkRowVC](buttonsStack) {
		(a, b) => a._2.link.id == b._2.link.id } { case (c, link) => new LinkRowVC(c, link, classManager) }
	
	private val view = Stack.buildColumnWithContext(isRelated = true) { mainStack =>
		context.forTextComponents().use { implicit textC =>
			mainStack += GroupHeader("Links")
		}
		mainStack += buttonsStack
		// When add link button is pressed, displays a dialog and inserts newly created link to DB and displayed data
		val addLinkButton = context.forTextComponents(Center).forSecondaryColorButtons.use { implicit btnC =>
			ImageAndTextButton.contextual(Icons.addBox.inButton, "Add Link") {
				parentWindow.foreach { window =>
					val classToEdit = _content
					new EditLinkDialog(None, classToEdit.classData, classManager.linkableClasses(classToEdit.classId))
						.display(window).foreach { _.foreach { newLink => classManager.addNewLink(newLink)
					} } }
			}.alignedToSide(Direction2D.Right, useLowPriorityLength = true)
		}
		mainStack += addLinkButton
	}
	
	
	// INITIAL CODE	------------------------
	
	manager.content = initialClass.links.map { initialClass.classData -> _ }
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: DisplayedClass) =
	{
		_content = newContent
		manager.content = newContent.links.map { newContent.classData -> _ }
	}
	
	override def content = _content
}
