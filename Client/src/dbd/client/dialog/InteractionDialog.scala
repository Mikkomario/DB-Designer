package dbd.client.dialog

import java.awt.Window

import utopia.reflection.shape.LengthExtensions._
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.window.Dialog
import utopia.reflection.container.swing.Stack
import utopia.reflection.container.swing.window.WindowResizePolicy.Program
import utopia.reflection.localization.{LocalizedString, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * A common trait for dialogs that are used for interacting (requesting some sort of input) with the user
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 */
trait InteractionDialog[A]
{
	// ABSTRACT	-----------------------
	
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
	 * @param baseCB Component creation context (builder, implicit)
	 * @param defaultLanguageCode ISO-code of the default language used (implicit)
	 * @param localizer A localizer (implicit)
	 * @param margins Margins used (implicit)
	 * @param colorScheme Color scheme used (implicit)
	 * @param exc Execution context (implicit)
	 * @return A future of the closing of the dialog, with a selected result (or default if none was selected)
	 */
	def display(parentWindow: Window)
			   (implicit baseCB: ComponentContextBuilder, defaultLanguageCode: String, localizer: Localizer,
				margins: Margins, colorScheme: ColorScheme, exc: ExecutionContext) =
	{
		// Creates the buttons based on button info
		val actualizedButtons = buttonData.map { buttonData =>
			buttonData -> ImageAndTextButton.contextualWithoutAction(buttonData.images, buttonData.rawText)(
				baseCB.withColors(buttonData.color).result)
		}
		
		// Creates dialog content
		implicit val baseContext: ComponentContext = baseCB.result
		val content = Stack.buildColumnWithContext() { mainStack =>
			mainStack += dialogContent
			mainStack += Stack.buildRowWithContext() { buttonRow =>
				actualizedButtons.foreach { buttonRow += _._2 }
			}.alignedToSide(Direction2D.Right)
		}.framed(margins.medium.any.square, colorScheme.gray.light)
		
		// Creates and sets up the dialog
		val dialog = new Dialog(parentWindow, content, title, Program)
		if (actualizedButtons.nonEmpty)
			dialog.registerButtons(actualizedButtons.head._2, actualizedButtons.drop(1).map { _._2 }: _*)
		dialog.setToCloseOnEsc()
		
		// Adds actions to dialog buttons
		var result: Option[A] = None
		actualizedButtons.foreach { case (data, button) => button.registerAction(() =>
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
