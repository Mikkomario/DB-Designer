package dbd.client.vc

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.EditClassDialog
import dbd.client.model.{Fonts, ParentOrSubClass}
import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.{CollectionView, Stack}
import utopia.reflection.controller.data.ContainerContentManager
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.Margins
import utopia.reflection.util.ComponentContextBuilder

import scala.concurrent.ExecutionContext

/**
 * Used for displaying data about multiple classes at a time
 * @author Mikko Hilpinen
 * @since 17.1.2020, v0.1
 */
class ClassesVC(targetHeight: Double, initialDatabaseId: Int)
			   (implicit margins: Margins, baseCB: ComponentContextBuilder, fonts: Fonts, colorScheme: ColorScheme,
				defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Int]
{
	// ATTRIBUTES	---------------------
	
	private val dataManager = new ClassDisplayManager(initialDatabaseId)
	private val addClassButton = ImageAndTextButton.contextual(Icons.addBox.forButtonWithBackground(colorScheme.secondary.dark),
		"Add Class") { () => addButtonPressed() }(baseCB.withColors(colorScheme.secondary.dark).result)
	private val classView = new CollectionView[ClassVC](Direction2D.Down, targetHeight, margins.medium.downscaling)
	private val displayManager = new ContainerContentManager[ParentOrSubClass, CollectionView[ClassVC], ClassVC](classView)(
		c => new ClassVC(c, dataManager))
	private val view = Stack.buildColumnWithContext() { stack =>
		stack += classView
		stack += addClassButton.alignedToSide(Direction2D.Right, useLowPriorityLength = true)
	}(baseCB.result)
	
	
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
