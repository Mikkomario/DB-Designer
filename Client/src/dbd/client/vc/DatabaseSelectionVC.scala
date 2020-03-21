package dbd.client.vc

import dbd.core.model.enumeration.NamingConvention._
import dbd.client.controller.{DatabasesManager, Icons}
import dbd.client.dialog.EditDatabaseDialog
import dbd.client.view.Fields
import dbd.core.model.existing.Database
import utopia.genesis.color.Color
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.input.SelectableWithPointers
import utopia.reflection.component.swing.button.ImageButton
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.{DisplayFunction, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
  * Used for selecting currently modified database and editing database configurations
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class DatabaseSelectionVC(buttonColor: Color)
						 (implicit exc: ExecutionContext, baseCB: ComponentContextBuilder, localizer: Localizer,
						  colorScheme: ColorScheme, margins: Margins)
	extends StackableAwtComponentWrapperWrapper with SelectableWithPointers[Database, Vector[Database]]
{
	// ATTRIBUTES	-------------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	private implicit val lang: String = "en"
	
	private val dataManager = new DatabasesManager()
	/*
	val selection =
	{
		// Creates the no results view first
		val searchPointer = new PointerWithEvents[Option[String]](None)
		val addViewBG = colorScheme.gray.default
		val buttonBG = colorScheme.secondary.forBackground(addViewBG)
		val addButton = ImageAndTextButton.contextual(Icons.addBox.forButtonWithBackground(buttonBG),
			"Create New Database") { () => displayAddDBDialog(searchPointer.value) }(baseCB.withColors(buttonBG).result)
		val noResultsLabel = SearchFromField.noResultsLabel("No database found with name '%s'", searchPointer)(
			baseCB.withTextColor(addViewBG.defaultTextColor).result)
		val noResultsView = Stack.buildColumnWithContext() { stack =>
			stack += noResultsLabel
			stack += addButton
		}.framed(margins.medium.any x margins.small.any, addViewBG)
		
		// Then the search & select field itself
		SearchFromField.contextualWithTextOnly[Database]("Select Database", noResultsView,
			displayFunction = DisplayFunction.noLocalization[Database] { _.configuration.name.toCapitalized },
			contentPointer = dataManager.contentPointer, searchFieldPointer = searchPointer,
			checkEquals = (a, b) => a.id == b.id)
	}*/
	private val selectionDD = Fields.dropDownWithPointer[Database](dataManager.contentPointer,
		"No databases to select from", "Select Database",
		DisplayFunction.noLocalization[Database] { _.configuration.name.toCapitalized }, colorScheme.gray.light,
		_.id == _.id)
	
	private val view = Stack.buildRowWithContext(isRelated = true) { stack =>
		stack += selectionDD
		stack += ImageButton.contextual(Icons.add.forButtonWithoutText(buttonColor)) { () => displayAddDBDialog() }
		stack += ImageButton.contextual(Icons.edit.forButtonWithoutText(buttonColor)) { () =>
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
				dataManager.addNewDatabase(newConfig)
			} }
		}
	}
}
