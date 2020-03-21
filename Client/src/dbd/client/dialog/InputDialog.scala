package dbd.client.dialog

import utopia.flow.util.TimeExtensions._
import utopia.reflection.shape.LengthExtensions._
import dbd.client.controller.Icons
import utopia.flow.util.WaitUtils
import utopia.genesis.handling.KeyStateListener
import utopia.genesis.shape.Axis.X
import utopia.genesis.shape.shape2D.{Direction2D, Point}
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.AwtComponentRelated
import utopia.reflection.component.swing.button.ImageButton
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.component.{ComponentLike, Focusable}
import utopia.reflection.container.stack.StackLayout.Center
import utopia.reflection.container.stack.segmented.SegmentedGroup
import utopia.reflection.container.swing.window.Popup
import utopia.reflection.container.swing.{SegmentedRow, Stack}
import utopia.reflection.localization.{LocalizedString, Localizer}
import utopia.reflection.shape.{Alignment, Margins}
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * This dialog provides a number of inputs for the user to interact with
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 */
abstract class InputDialog[A](implicit colorScheme: ColorScheme, baseCB: ComponentContextBuilder, margins: Margins,
							  exc: ExecutionContext, localizer: Localizer) extends InteractionDialog[A]
{
	// ATTRIBUTES	---------------------
	
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
	
	// Input dialogs have OK and Cancel buttons
	override protected def buttonData =
	{
		val okButton = new DialogButtonInfo[A]("OK", Icons.checkCircle, colorScheme.secondary, () =>
		{
			// Checks the results. If failed, returns focus to an item and displays a message
			produceResult match
			{
				case Right(result) => Some(result) -> true
				case Left(redirect) =>
					val colors = colorScheme.error
					redirect._1.requestFocusInWindow()
					implicit val context: ComponentContext = baseCB.withTextColor(colors.defaultTextColor).result
					val dismissButton = ImageButton.contextualWithoutAction(Icons.close.forButtonWithBackground(colors))
					val popupContent = Stack.buildRowWithContext(layout = Center) { row =>
						row += dismissButton
						row += TextLabel.contextual(redirect._2)
					}.framed(margins.medium.any.square, colors.background)
					val popup = Popup(redirect._1, popupContent, context.actorHandler,
						hideWhenFocusLost = false, Alignment.Left) { (cSize, pSize) =>
						Point(cSize.width + margins.medium, -(pSize.height - cSize.height) / 2) }
					dismissButton.registerAction(() => popup.close())
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
		implicit val context: ComponentContext = baseCB.withAlignment(Alignment.Right).result
		
		// Places rows in an aligned stack
		val group = new SegmentedGroup(X)
		Stack.buildColumnWithContext() { stack =>
			fields.foreach { row =>
				val fieldInRow =
				{
					if (row.spansWholeRow)
						row.field
					else
						row.field.alignedToSide(Direction2D.Left)
				}
				val rowComponent = SegmentedRow.partOfGroupWithItems(group, Vector(TextLabel.contextual(row.fieldName),
					fieldInRow), margin = margins.medium.downscaling)
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
