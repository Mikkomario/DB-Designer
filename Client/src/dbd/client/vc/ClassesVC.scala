package dbd.client.vc

import dbd.core.database
import dbd.client.controller.{ConnectionPool, Icons}
import dbd.client.dialog.EditClassDialog
import dbd.client.model.Fonts
import utopia.reflection.shape.LengthExtensions._
import dbd.core.model.existing.Class
import dbd.core.model.partial.NewClass
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.CollectionView
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
class ClassesVC(targetHeight: Double)
			   (implicit margins: Margins, baseCB: ComponentContextBuilder, fonts: Fonts, colorScheme: ColorScheme,
				defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Vector[(Class, Boolean)]]
{
	// ATTRIBUTES	---------------------
	/*
	private val addClassButton = ImageAndTextButton.contextual(Icons.addBox.forButtonWithBackground(colorScheme.secondary),
		"Add Class") { () => addButtonPressed() }(baseCB.withColors(colorScheme.secondary).result)
		*/
	private val view = new CollectionView[ClassVC](Direction2D.Down, targetHeight, margins.medium.downscaling)
	private val manager = new ContainerContentManager[(Class, Boolean), CollectionView[ClassVC], ClassVC](view)(
		c => new ClassVC(c._1, c._2))
	
	
	// IMPLEMENTED	---------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: Vector[(Class, Boolean)]) = manager.content = newContent
	
	override def content = manager.content
	
	
	// OTHER	-------------------------
	
	/*
	private def addButtonPressed() =
	{
		parentWindow.foreach { window =>
			new EditClassDialog().display(window).foreach { _.foreach { newClassInfo =>
				ConnectionPool.tryWith { implicit connection =>
					content :+= database.Classes.insert(NewClass(newClassInfo, Vector()))
				}
			} }
		}
	}*/
}
