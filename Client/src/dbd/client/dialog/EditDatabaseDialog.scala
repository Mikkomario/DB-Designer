package dbd.client.dialog

import dbd.core.model.partial.database.DatabaseConfigurationData
import utopia.reflection.component.swing.TextField
import utopia.reflection.shape.LengthExtensions._

/**
  * Used for adding / editing databases
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class EditDatabaseDialog(databaseToEdit: Option[DatabaseConfigurationData] = None, newDBName: Option[String] = None)
	extends InputDialog[Option[DatabaseConfigurationData]]
{
	// ATTRIBUTES	-----------------------
	
	import dbd.client.view.DefaultContext._
	
	private implicit val language: String = "en"
	
	private val nameField = baseContext.inContextWithBackground(dialogBackground).forTextComponents()
		.forGrayFields.use { implicit c => TextField.contextual(standardInputWidth.any, initialText =
		databaseToEdit.map { _.name }.orElse(newDBName).getOrElse(""), prompt = Some("Name for your database")) }
	
	
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
					Right(Some(DatabaseConfigurationData(dbName))) // TODO: Set creator id
			case None => Left(nameField, "Please specify database name first")
		}
	}
	
	override protected def defaultResult = None
	
	override protected def title = if (databaseToEdit.isDefined) "Edit Database" else "Add Database"
}
