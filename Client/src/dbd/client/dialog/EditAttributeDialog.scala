package dbd.client.dialog

import dbd.core.model.partial.NewAttributeConfiguration
import dbd.core.model.AttributeType
import dbd.core.model.existing.Attribute
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.{DropDown, Switch, TextField}
import utopia.reflection.localization.{DisplayFunction, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Used for editing and adding new attributes
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
class EditAttributeDialog(attributeToEdit: Option[Attribute] = None)
						 (implicit baseCB: ComponentContextBuilder, defaultLanguageCode: String, localizer: Localizer,
						  margins: Margins, colorScheme: ColorScheme, exc: ExecutionContext)
	extends InputDialog[Option[NewAttributeConfiguration]]
{
	// ATTRIBUTES	-----------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private val nameField = TextField.contextual(initialText = attributeToEdit.map { _.configuration.name }.getOrElse(""),
		prompt = Some("Name for the attribute"))
	private val typeSelectionField = DropDown.contextual[AttributeType]("Select a data type",
		DisplayFunction.localized(), AttributeType.values)
	private val optionalSwitch = Switch.contextual
	private val searchKeySwitch = Switch.contextual
	
	
	// INITIAL CODE	-----------------------
	
	// Sets selections based on existing attribute
	attributeToEdit.foreach { a =>
		typeSelectionField.selectOne(a.configuration.dataType)
		optionalSwitch.isOn = a.configuration.isOptional
		searchKeySwitch.isOn = a.configuration.isSearchKey
	}
	
	
	// IMPLEMENTED	------------------------
	
	override protected def fields = Vector(
		InputRowInfo("Attribute Name", nameField),
		InputRowInfo("Attribute Type", typeSelectionField),
		InputRowInfo("Optional", optionalSwitch, spansWholeRow = false),
		InputRowInfo("Search Key", searchKeySwitch, spansWholeRow = false))
	
	override protected def produceResult =
	{
		nameField.value match
		{
			case Some(newName) =>
				typeSelectionField.value match
				{
					case Some(newType) =>
						val newConfig = NewAttributeConfiguration(newName, newType, optionalSwitch.isOn, searchKeySwitch.isOn)
						if (!attributeToEdit.exists { _.configuration ~== newConfig })
							Right(Some(newConfig))
						else
							Right(None)
					case None => Left(typeSelectionField, "Please select a type for the attribute")
				}
			case None => Left(nameField, "Attribute name is required")
		}
	}
	
	override protected def title = if (attributeToEdit.isDefined) "Edit Attribute" else "Add Attribute"
	
	override protected def defaultResult = None
}
