package dbd.client.vc

import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.Fonts
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.container.swing.{AwtContainerRelated, Stack}
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.{Margins, StackLength}
import utopia.reflection.util.{ComponentContextBuilder, Screen}

import scala.concurrent.ExecutionContext

/**
  * This VC controls the main view
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class MainVC(implicit baseCB: ComponentContextBuilder, exc: ExecutionContext, localizer: Localizer, margins: Margins,
			 fonts: Fonts, colorScheme: ColorScheme)
	extends StackableAwtComponentWrapperWrapper with AwtContainerRelated
{
	// ATTRIBUTES	-----------------------
	
	private val dbVC = new DatabaseSelectionVC(colorScheme.primary.dark.defaultTextColor)
	private val classesVC = new ClassesVC(Screen.height * 0.7, dbVC.value.id)
	
	private val view = Stack.buildColumn(margin = 0.fixed) { stack =>
		// Adds header that contains the database selection
		stack += dbVC.alignedToSide(Direction2D.Left, useLowPriorityLength = true).framed(
			margins.medium.downscaling x StackLength(margins.small, margins.medium), colorScheme.primary.dark)
		// Adds the main content
		stack += classesVC.alignedToSide(Direction2D.Left, useLowPriorityLength = true).framed(
			margins.medium.any.square, colorScheme.primary.light)
	}
	
	
	// INITIAL CODE	------------------------
	
	// Whenever database selection changes, informs the main view
	dbVC.addValueListener { e => classesVC.content = e.newValue.id }
	
	
	// IMPLEMENTED	------------------------
	
	override def component = view.component
	
	override protected def wrapped = view
}
