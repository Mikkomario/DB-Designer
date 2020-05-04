package dbd.client.dialog

import java.awt.Window

import dbd.client.view.DefaultContext
import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ComponentColor
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.Stack
import utopia.reflection.container.swing.window.WindowResizePolicy.Program
import utopia.reflection.container.swing.window.dialog.Dialog
import utopia.reflection.localization.LocalizedString

object InteractionDialog
{
	/**
	  * Dialog background used by default
	  */
	val defaultDialogBackground = DefaultContext.colorScheme.primary
}

/**
 * A common trait for dialogs that are used for interacting (requesting some sort of input) with the user
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 */
trait InteractionDialog[A]
{
	// ABSTRACT	-----------------------
	
	/**
	  * @return Background color used in this dialog
	  */
	protected def dialogBackground: ComponentColor
	
	/**
	 * Buttons that are displayed on this dialog. The first button is used as the default.
	 */
	protected def buttonData: Vector[DialogButtonInfo[A]]
	
	/**
	 * Dialog body element(s)
	 */
	protected def dialogContent: AwtStackable
	
	/**
	 * @return Result provided when no result is gained through interacting with the buttons
	 */
	protected def defaultResult: A
	
	/**
	 * @return Title displayed on this dialog
	 */
	protected def title: LocalizedString
	
	
	// OTHER	-----------------------
	
	/**
	 * Displays an interactive dialog to the user
	 * @param parentWindow Window that will "own" the new dialog
	 * @return A future of the closing of the dialog, with a selected result (or default if none was selected)
	 */
	def display(parentWindow: Window) =
	{
		import DefaultContext._
		
		// Creates dialog content
		val (buttons, content) = baseContext.inContextWithBackground(dialogBackground).use { context =>
			// Creates the buttons based on button info
			val actualizedButtons = buttonData.map { buttonData =>
				context.forTextComponents().forCustomColorButtons(buttonData.backgroundColor(context)).use { implicit btnC =>
					buttonData -> ImageAndTextButton.contextualWithoutAction(buttonData.images, buttonData.text)
				}
			}
			// Places content in a stack
			val content = context.use { implicit baseC =>
				Stack.buildColumnWithContext() { mainStack =>
					mainStack += dialogContent
					mainStack += Stack.buildRowWithContext() { buttonRow =>
						actualizedButtons.foreach { buttonRow += _._2 }
					}.alignedToSide(Direction2D.Right)
				}
			}.framed(context.margins.medium.downscaling, context.containerBackground)
			actualizedButtons -> content
		}
		
		// Creates and sets up the dialog
		val dialog = new Dialog(parentWindow, content, title, Program)
		if (buttons.nonEmpty)
			dialog.registerButtons(buttons.head._2, buttons.drop(1).map { _._2 }: _*)
		dialog.setToCloseOnEsc()
		
		// Adds actions to dialog buttons
		var result: Option[A] = None
		buttons.foreach { case (data, button) => button.registerAction(() =>
		{
			val (newResult, shouldClose) = data.generateResultOnPress()
			result = newResult
			if (shouldClose)
				dialog.close()
		}) }
		
		// Displays the dialog and returns a promise of final result
		dialog.startEventGenerators(baseContext.actorHandler)
		dialog.display()
		dialog.closeFuture.map { _ => result.getOrElse(defaultResult) }
	}
}
