package dbd.client.dialog

import dbd.core.model.partial.NewDatabaseConfiguration
import dbd.core.model.template.DatabaseConfigurationLike
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.TextField
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
  * Used for adding / editing databases
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class EditDatabaseDialog(databaseToEdit: Option[DatabaseConfigurationLike] = None)
						(implicit baseCB: ComponentContextBuilder, colorScheme: ColorScheme, margins: Margins,
						 exc: ExecutionContext, localizer: Localizer)
	extends InputDialog[Option[NewDatabaseConfiguration]]
{
	// ATTRIBUTES	-----------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	private implicit val language: String = "en"
	
	private val nameField = TextField.contextual(initialText = databaseToEdit.map { _.name }.getOrElse(""),
		prompt = Some("Name for your database"))
	
	
	// IMPLEMENTED	-----------------------
	
	override protected def fields = Vector(InputRowInfo("Database Name", nameField))
	
	override protected def produceResult =
	{
		nameField.value match
		{
			case Some(dbName) =>
				if (databaseToEdit.exists { _.name == dbName })
					Right(None)
				else
					Right(Some(NewDatabaseConfiguration(dbName)))
			case None => Left(nameField, "Please specify database name first")
		}
	}
	
	override protected def defaultResult = None
	
	override protected def title = if (databaseToEdit.isDefined) "Edit Database" else "Add Database"
}
