package dbd.client.dialog

import dbd.core.model.existing.ClassInfo
import dbd.core.model.partial.NewClassInfo
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.{Switch, TextField}
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Used for editing class info
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 */
class EditClassDialog(classToEdit: Option[ClassInfo])
					 (implicit baseCB: ComponentContextBuilder, margins: Margins, colorScheme: ColorScheme,
					  exc: ExecutionContext, defaultLanguageCode: String, localizer: Localizer)
	extends InputDialog[Option[NewClassInfo]]
{
	// ATTRIBUTES	---------------------
	
	private implicit val componentContext: ComponentContext = baseCB.result
	
	private val classNameField = TextField.contextual(initialText = classToEdit.map { _.name }.getOrElse(""),
		prompt = Some("Name for the class"))
	private val isMutableSwitch = Switch.contextual
	
	
	// INITIAL CODE	---------------------
	
	classToEdit.foreach { c => isMutableSwitch.value = c.isMutable }
	
	
	// IMPLEMENTED	---------------------
	
	override protected def fields = Vector(
		InputRowInfo("Class Name", classNameField),
		InputRowInfo("Mutable", isMutableSwitch, spansWholeRow = false))
	
	override protected def produceResult = classNameField.value match
	{
		case Some(className) =>
			if (classToEdit.exists { c => c.name == className && c.isMutable == isMutableSwitch.value })
				Right(None)
			else
				Right(Some(NewClassInfo(className, isMutableSwitch.value)))
		case None => Left(classNameField, "Please specify a name for the class")
	}
	
	override protected def defaultResult = None
	
	override protected def title = if (classToEdit.isDefined) "Edit Class" else "Add Class"
}
