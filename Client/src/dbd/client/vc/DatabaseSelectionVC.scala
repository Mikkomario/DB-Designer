package dbd.client.vc

import dbd.core.model.enumeration.NamingConvention._
import dbd.client.controller.{DatabasesManager, Icons}
import dbd.client.dialog.EditDatabaseDialog
import dbd.client.view.Fields
import dbd.core.model.existing.database.Database
import dbd.core.model.partial.database.DatabaseData
import utopia.reflection.component.context.ColorContext
import utopia.reflection.component.template.input.SelectableWithPointers
import utopia.reflection.component.swing.button.ImageButton
import utopia.reflection.component.swing.template.StackableAwtComponentWrapperWrapper
import utopia.reflection.container.swing.layout.multi.Stack
import utopia.reflection.localization.DisplayFunction

/**
  * Used for selecting currently modified database and editing database configurations
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class DatabaseSelectionVC(implicit parentContext: ColorContext)
	extends StackableAwtComponentWrapperWrapper with SelectableWithPointers[Database, Vector[Database]]
{
	import dbd.client.view.DefaultContext._
	
	// ATTRIBUTES	-------------------------
	
	private implicit val lang: String = "en"
	
	private val dataManager = new DatabasesManager()
	
	private val selectionDD = parentContext.forTextComponents().forGrayFields.use { implicit c =>
		Fields.dropDownWithPointer[Database](dataManager.contentPointer,
			"No databases to select from", "Select Database",
			DisplayFunction.noLocalization[Database] { _.configuration.name.toCapitalized },
			_.id == _.id, contentIsStateless = false)
	}
	
	private val view = Stack.buildRowWithContext(isRelated = true) { stack =>
		stack += selectionDD
		stack += ImageButton.contextual(Icons.add.asIndividualButton) { displayAddDBDialog() }
		stack += ImageButton.contextual(Icons.edit.asIndividualButton) {
			parentWindow.foreach { window =>
				val dbToEdit = value
				new EditDatabaseDialog(Some(dbToEdit.configuration)).display(window).foreach { _.foreach { newConfig =>
					dataManager.editDatabase(dbToEdit.id, newConfig)
				} }
			}
		}
	}
	
	
	// INITIAL CODE	-------------------------
	
	selectionDD.selectOne(dataManager.value)
	
	// When user selects a different DB from the DD, informs the data manager (also updates selection based on
	// data manager changes)
	selectionDD.addValueListener { _.newValue.foreach { dataManager.value = _ } }
	dataManager.addValueListener { e => selectionDD.selectOne(e.newValue) }
	
	
	// IMPLEMENTED	-------------------------
	
	override protected def wrapped = view
	
	override def contentPointer = dataManager.contentPointer
	
	override def valuePointer = dataManager.valuePointer
	
	
	// OTHER	-----------------------------
	
	private def displayAddDBDialog(initialDBName: Option[String] = None) =
	{
		parentWindow.foreach { window =>
			new EditDatabaseDialog(newDBName = initialDBName).display(window).foreach { _.foreach { newConfig =>
				// FIXME: Set owner organization id
				val newDB = DatabaseData(???, newConfig)
				dataManager.addNewDatabase(newDB)
			} }
		}
	}
}
