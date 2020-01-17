package dbd.client.vc

import dbd.client.model.Fonts
import utopia.reflection.shape.LengthExtensions._
import dbd.core.model.existing.Class
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
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
	extends StackableAwtComponentWrapperWrapper with Refreshable[Vector[Class]]
{
	// ATTRIBUTES	---------------------
	
	private val view = new CollectionView[ClassVC](Direction2D.Down, targetHeight, margins.medium.downscaling)
	private val manager = new ContainerContentManager[Class, CollectionView[ClassVC], ClassVC](view)(c => new ClassVC(c))
	
	
	// IMPLEMENTED	---------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: Vector[Class]) = manager.content = newContent
	
	override def content = manager.content
}
