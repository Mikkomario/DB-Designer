package dbd.client.dialog

import utopia.flow.util.TimeExtensions._
import utopia.reflection.shape.LengthExtensions._
import dbd.client.controller.Icons
import utopia.flow.util.WaitUtils
import utopia.genesis.handling.KeyStateListener
import utopia.genesis.shape.shape2D.{Direction2D, Point}
import utopia.reflection.component.swing.template.AwtComponentRelated
import utopia.reflection.component.swing.button.ImageButton
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.component.template.{ComponentLike, Focusable}
import utopia.reflection.container.stack.StackLayout.Center
import utopia.reflection.container.swing.layout.SegmentGroup
import utopia.reflection.container.swing.layout.multi.Stack
import utopia.reflection.container.swing.window.Popup
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.Alignment

/**
 * This dialog provides a number of inputs for the user to interact with
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 */
// TODO: Extend or replace with the Reflection counterpart
abstract class InputDialog[A] extends InteractionDialog[A]
{
	// ATTRIBUTES	---------------------
	
	import dbd.client.view.DefaultContext._
	
	private implicit val language: String = "en"
	
	
	// ABSTRACT	-------------------------
	
	/**
	 * @return Fields that are used in producing input in this dialog, along with some additional information
	 */
	protected def fields: Vector[InputRowInfo]
	
	/**
	 * Produces a result based on dialog input when the user selects "OK"
	 * @return Either the produced input (right) or a field to return the focus to, along with a message
	 *         to display to the user (left)
	 */
	protected def produceResult: Either[(Focusable with ComponentLike with AwtComponentRelated, LocalizedString), A]
	
	
	// IMPLEMENTED	---------------------
	
	override protected def dialogBackground = InteractionDialog.defaultDialogBackground
	
	// Input dialogs have OK and Cancel buttons
	override protected def buttonData =
	{
		val okButton = new DialogButtonInfo[A]("OK", Icons.checkCircle, Right(false), () =>
		{
			// Checks the results. If failed, returns focus to an item and displays a message
			produceResult match
			{
				case Right(result) => Some(result) -> true
				case Left(redirect) =>
					// Creates the notification pop-up
					val popup = baseContext.inContextWithBackground(colorScheme.error).forTextComponents().use { implicit popupContext =>
						val dismissButton = ImageButton.contextualWithoutAction(Icons.close.asIndividualButton)
						val popupContent = Stack.buildRowWithContext(layout = Center) { row =>
							row += dismissButton
							row += TextLabel.contextual(redirect._2)
						}.framed(margins.medium.any, colorScheme.error)
						val popup = Popup(redirect._1, popupContent, popupContext.actorHandler,
							hideWhenFocusLost = false, Alignment.Left) { (cSize, pSize) =>
							Point(cSize.width + margins.medium, -(pSize.height - cSize.height) / 2) }
						dismissButton.registerAction(() => popup.close())
						
						popup
					}
					
					// Closes the pop-up if any key is pressed or after a delay
					popup.addKeyStateListener(KeyStateListener.onAnyKeyPressed { _ => popup.close() })
					WaitUtils.delayed(5.seconds) { popup.close() }
					popup.display(false)
					None -> false
			}
		})
		Vector(okButton, DialogButtonInfo.cancel("Cancel"))
	}
	
	override protected def dialogContent =
	{
		baseContext.inContextWithBackground(dialogBackground).forTextComponents(Alignment.Right).use { implicit fieldContext =>
			// Places rows in an aligned stack
			val group = new SegmentGroup()
			Stack.buildColumnWithContext() { stack =>
				fields.foreach { row =>
					val fieldInRow =
					{
						if (row.spansWholeRow)
							row.field
						else
							row.field.alignedToSide(Direction2D.Left)
					}
					val rowComponent = Stack.rowWithItems(group.wrap(Vector(TextLabel.contextual(row.fieldName), fieldInRow)),
						margins.medium.downscaling)
					// Some rows have dependent visibility state
					row.rowVisibilityPointer.foreach { pointer =>
						rowComponent.isVisible = pointer.value
						pointer.addListener { e => rowComponent.isVisible = e.newValue }
					}
					stack += rowComponent
				}
			}
		}
	}
}
