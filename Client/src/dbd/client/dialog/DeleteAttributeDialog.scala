package dbd.client.dialog

import java.awt.Window

import utopia.reflection.shape.LengthExtensions._
import dbd.client.controller.Icons
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.component.swing.MultiLineTextView
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.container.swing.Stack
import utopia.reflection.container.swing.window.Dialog
import utopia.reflection.localization.{LocalizedString, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ColorScheme, ComponentContext, ComponentContextBuilder, Screen}

import scala.concurrent.ExecutionContext

/**
 * Used for checking whether the user really wants to delete the specified attribute
 * @author Mikko Hilpinen
 * @since 13.1.2020, v0.1
 */
// TODO: This class contains a lot of copy-paste between this and EditAttributeDialog, once a third dialog is being added,
// combine these into one or more traits
class DeleteAttributeDialog(parentWindow: Window, attributeName: String)(implicit baseCB: ComponentContextBuilder,
																		 defaultLanguageCode: String, localizer: Localizer,
																		 colorScheme: ColorScheme, margins: Margins)
{
	// ATTRIBUTES	------------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private var selectedResult = false
	
	private val okButton = ImageAndTextButton.contextualWithoutAction(Icons.delete.forDarkButtons, "Yes")(
		baseCB.withBackground(colorScheme.error).result)
	private val cancelButton = ImageAndTextButton.contextualWithoutAction(Icons.close.forDarkButtons, "No")(
		baseCB.withBackground(colorScheme.primary).result)
	
	private val view = Stack.buildColumnWithContext() { mainStack =>
		val question: LocalizedString = "Are you sure you wish to permanently delete attribute '%s'?"
		mainStack += MultiLineTextView.contextual(question.interpolate(attributeName), Screen.size.width / 3)
		mainStack += Stack.buildRowWithContext() { buttonRow =>
			buttonRow += okButton
			buttonRow += cancelButton
		}.alignedToSide(Direction2D.Right)
	}.framed(margins.medium.any.square, colorScheme.gray)
	
	private val dialog = new Dialog(parentWindow, view, "Delete Attribute")
	
	
	// INITIAL CODE	------------------------
	
	okButton.registerAction(() =>
	{
		selectedResult = true
		dialog.close()
	})
	cancelButton.registerAction(() => dialog.close())
	dialog.setToCloseOnEsc()
	
	
	// OTHER	----------------------------
	
	/**
	 * Displays this dialog
	 * @param exc Implicit execution context
	 * @return The eventual choice made by the user (true if user pressed yes, otherwise false)
	 */
	def display()(implicit exc: ExecutionContext) =
	{
		dialog.startEventGenerators(baseContext.actorHandler)
		dialog.display()
		dialog.closeFuture.map { _ => selectedResult }
	}
}
