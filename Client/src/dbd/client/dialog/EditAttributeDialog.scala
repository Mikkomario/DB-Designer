package dbd.client.dialog

import dbd.client.controller.Icons
import dbd.core.model.partial.NewAttributeConfiguration
import utopia.reflection.shape.LengthExtensions._
import dbd.core.model.AttributeType
import dbd.core.model.existing.Attribute
import utopia.genesis.color.Color
import utopia.genesis.shape.Axis.X
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.component.swing.button.ImageAndTextButton
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.component.swing.{DropDown, Switch, TextField}
import utopia.reflection.container.stack.StackLayout.Trailing
import utopia.reflection.container.stack.segmented.SegmentedGroup
import utopia.reflection.container.swing.{SegmentedRow, Stack}
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.window.WindowResizePolicy.Program
import utopia.reflection.container.swing.window.Dialog
import utopia.reflection.localization.{DisplayFunction, Localizer}
import utopia.reflection.shape.{Alignment, Margins}
import utopia.reflection.util.{ColorScheme, ComponentContext, ComponentContextBuilder}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Used for editing and adding new attributes
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
class EditAttributeDialog(parentWindow: java.awt.Window, attributeToEdit: Option[Attribute] = None)
						 (implicit baseCB: ComponentContextBuilder, defaultLanguageCode: String, localizer: Localizer,
						  margins: Margins, colorScheme: ColorScheme)
{
	// ATTRIBUTES	-----------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private val nameField = TextField.contextual(initialText = attributeToEdit.map { _.configuration.name }.getOrElse(""),
		prompt = Some("Name for the attribute"))
	private val typeSelectionField = DropDown.contextual[AttributeType]("Select a data type",
		DisplayFunction.localized(), AttributeType.values)
	private val optionalSwitch = Switch.contextual
	private val searchKeySwitch = Switch.contextual
	
	private val okButton = ImageAndTextButton.contextualWithoutAction(Icons.checkCircle.forLightButtons,
		"Ok")
	private val cancelButton = ImageAndTextButton.contextualWithoutAction(Icons.close.forDarkButtons,
		"Cancel")(baseCB.copy(background = Some(colorScheme.primary), textColor = Color.white).result)
	
	private val dialogContent = Stack.buildColumnWithContext(layout = Trailing) { mainStack =>
		mainStack += Stack.buildColumnWithContext() { inputStack =>
			implicit val group: SegmentedGroup = new SegmentedGroup(X)
			inputStack += makeRow("Attribute Name", nameField)
			inputStack += makeRow("Attribute Type", typeSelectionField)
			inputStack += makeLeftAlignedRow("Optional", optionalSwitch)
			inputStack += makeLeftAlignedRow("Search Key", searchKeySwitch)
		}
		mainStack += Stack.buildRowWithContext() { buttonRow =>
			buttonRow += okButton
			buttonRow += cancelButton
		}
	}.framed(margins.medium.any.square, colorScheme.gray.light)
	
	private val window = new Dialog(parentWindow, dialogContent,
		if (attributeToEdit.isDefined) "Edit Attribute" else "Add Attribute", Program)
	
	private var _editedConfig: Option[NewAttributeConfiguration] = None
	
	
	// INITIAL CODE	-----------------------
	
	// Sets selections based on existing attribute
	attributeToEdit.foreach { a =>
		typeSelectionField.selectOne(a.configuration.dataType)
		optionalSwitch.isOn = a.configuration.isOptional
		searchKeySwitch.isOn = a.configuration.isSearchKey
	}
	
	// Handles buttons
	// Ok button saves edited configuration (but only if changed)
	okButton.registerAction(() =>
	{
		nameField.value match
		{
			case Some(newName) =>
				typeSelectionField.value match
				{
					case Some(newType) =>
						val newConfig = NewAttributeConfiguration(newName, newType, optionalSwitch.isOn, searchKeySwitch.isOn)
						if (!attributeToEdit.exists { _.configuration ~== newConfig })
							_editedConfig = Some(newConfig)
						window.close()
					case None =>
						typeSelectionField.requestFocus()
						// TODO: Again, display pop-up
				}
			case None =>
				nameField.requestFocus()
				// TODO: Display pop-up or something to indicate that the value is required
		}
	})
	cancelButton.registerAction(() => window.close())
	window.registerButtons(okButton, cancelButton)
	window.setToCloseOnEsc()
	
	
	// COMPUTED	----------------------------
	
	/*
	 * @return An edited configuration result from this dialog
	 */
	// def editedConfig = _editedConfig
	
	
	// OTHER	----------------------------
	
	/**
	 * Displays this dialog
	 * @param exc Implicit execution context
	 * @return A future of this dialog's closing. Will include possibly edited attribute configuration
	 *         (None if cancelled or not edited)
	 */
	def display()(implicit exc: ExecutionContext): Future[Option[NewAttributeConfiguration]] =
	{
		window.startEventGenerators(baseContext.actorHandler)
		window.display()
		window.closeFuture.map { _ => _editedConfig }
	}
	
	private def makeRow(fieldName: String, field: AwtStackable)(implicit group: SegmentedGroup) =
	{
		SegmentedRow.partOfGroupWithItems(group, Vector(TextLabel.contextual(fieldName)(
			baseCB.withAlignment(Alignment.Right).result), field), margin = margins.medium.downscaling)
	}
	
	private def makeLeftAlignedRow(fieldName: String, field: AwtStackable)(implicit group: SegmentedGroup) =
		makeRow(fieldName, field.alignedToSide(Direction2D.Left, useLowPriorityLength = true))
}
