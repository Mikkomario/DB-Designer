package dbd.client.dialog

import dbd.core.model.existing.database.ClassInfo
import dbd.core.model.partial.database.NewClassInfo
import utopia.reflection.component.context.ButtonContext
import utopia.reflection.component.swing.{Switch, TextField}
import utopia.reflection.shape.LengthExtensions._

/**
 * Used for editing class info
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 */
class EditClassDialog(classToEdit: Option[ClassInfo] = None) extends InputDialog[Option[NewClassInfo]]
{
	// ATTRIBUTES	---------------------
	
	import dbd.client.view.DefaultContext._
	
	private implicit val languageCode: String = "en"
	private implicit val context: ButtonContext = baseContext.inContextWithBackground(dialogBackground)
		.forTextComponents().forGrayFields
	
	private val classNameField = TextField.contextual(standardInputWidth.any, initialText = classToEdit.map { _.name }.getOrElse(""),
		prompt = Some("Name for the class"))
	private val isMutableSwitch = Switch.contextual(switchWidth)
	
	
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
