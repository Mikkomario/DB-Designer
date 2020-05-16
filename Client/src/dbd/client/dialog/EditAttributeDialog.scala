package dbd.client.dialog

import dbd.client.controller.Icons
import dbd.client.view.Fields
import dbd.core.model.enumeration.AttributeType
import dbd.core.model.existing.database.Attribute
import dbd.core.model.partial.database
import dbd.core.model.partial.database.NewAttributeConfiguration
import utopia.reflection.component.context.ButtonContext
import utopia.reflection.component.swing.{Switch, TextField}
import utopia.reflection.localization.DisplayFunction
import utopia.reflection.shape.LengthExtensions._

/**
 * Used for editing and adding new attributes
 * @author Mikko Hilpinen
 * @since 12.1.2020, v0.1
 */
class EditAttributeDialog(attributeToEdit: Option[Attribute] = None)
	extends InputDialog[Option[NewAttributeConfiguration]]
{
	import dbd.client.view.DefaultContext._
	
	// ATTRIBUTES	-----------------------
	
	private implicit val languageCode: String = "en"
	private implicit val context: ButtonContext = baseContext.inContextWithBackground(dialogBackground)
		.forTextComponents().forGrayFields
	
	private val nameField = TextField.contextual(standardInputWidth.any, initialText = attributeToEdit.map { _.configuration.name }.getOrElse(""),
		prompt = Some("Name for the attribute"))
	private val typeSelectionField = Fields.searchFromWithIcons[AttributeType]("No data type matching '%s'",
		"Select a data type", DisplayFunction.localized(), AttributeType.values) { t =>
		Icons.forAttributeType(t) }
	private val optionalSwitch = Switch.contextual(switchWidth)
	private val searchKeySwitch = Switch.contextual(switchWidth)
	
	
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
						val newConfig = database.NewAttributeConfiguration(newName, newType, optionalSwitch.isOn, searchKeySwitch.isOn)
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
