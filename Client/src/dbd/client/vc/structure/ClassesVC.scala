package dbd.client.vc.structure

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.EditClassDialog
import dbd.client.model.ParentOrSubClass
import utopia.genesis.shape.Axis.Y
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.{CollectionView, Stack}
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.shape.Alignment.Center
import utopia.reflection.shape.LengthExtensions._

/**
 * Used for displaying data about multiple classes at a time
 * @author Mikko Hilpinen
 * @since 17.1.2020, v0.1
 */
class ClassesVC(targetHeight: Double, initialDatabaseId: Int)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Int]
{
	// ATTRIBUTES	---------------------
	
	import dbd.client.view.DefaultContext._
	
	private implicit val language: String = "en"
	
	private val backgroundColor = colorScheme.primary.light
	private val buttonColor = colorScheme.secondary.forBackground(backgroundColor)
	
	private val dataManager = new ClassDisplayManager(initialDatabaseId)
	
	private val context = baseContext.inContextWithBackground(backgroundColor)
	
	private val addClassButton = context.forTextComponents(Center).forSecondaryColorButtons.use { implicit btnC =>
		ImageAndTextButton.contextual(Icons.addBox.inButton, "Add Class") { addButtonPressed() }
	}
	
	private val classView = new CollectionView[ClassVC](Y, targetHeight, margins.medium.downscaling)
	private val displayManager = ContainerContentManager.forImmutableStates[ParentOrSubClass, ClassVC](classView) {
		(a, b) => a.classId == b.classId } { c => new ClassVC(c, dataManager, context) }
	
	private val view = context.use { implicit c =>
		Stack.buildColumnWithContext() { stack =>
			stack += classView
			stack += addClassButton.alignedToSide(Direction2D.Right, useLowPriorityLength = true)
		}.alignedToSide(Direction2D.Left, useLowPriorityLength = true).framed(margins.medium.any.square, c.containerBackground)
	}
	
	
	// INITIAL CODE	---------------------
	
	displayManager.content = dataManager.content.map { ParentOrSubClass.topLevel }
	// Displays get updated whenever data source updates
	dataManager.addContentListener { e => displayManager.content = e.newValue.map { ParentOrSubClass.topLevel } }
	
	
	// IMPLEMENTED	---------------------
	
	override def content_=(newContent: Int) = dataManager.currentDatabaseId = newContent
	
	override def content = dataManager.currentDatabaseId
	
	override protected def wrapped = view
	
	
	// OTHER	-------------------------
	
	private def addButtonPressed() =
	{
		parentWindow.foreach { window =>
			new EditClassDialog().display(window).foreach { _.foreach { newClassInfo => dataManager.addNewClass(newClassInfo) } }
		}
	}
}
