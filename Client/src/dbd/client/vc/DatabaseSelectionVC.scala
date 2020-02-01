package dbd.client.vc

import dbd.core.model.enumeration.NamingConvention._
import dbd.client.controller.{DatabasesManager, Icons}
import dbd.client.dialog.EditDatabaseDialog
import dbd.core.model.existing.Database
import utopia.genesis.color.Color
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.input.SelectableWithPointers
import utopia.reflection.component.swing.button.ImageButton
import utopia.reflection.component.swing.{DropDown, StackableAwtComponentWrapperWrapper}
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
	
	private val selectionDD = DropDown.contextual("Select Database",
		DisplayFunction.noLocalization[Database] { _.configuration.name.toCapitalized }, dataManager.content)
	
	private val view = Stack.buildRowWithContext(isRelated = true) { stack =>
		stack += selectionDD
		stack += ImageButton.contextual(Icons.add.forButtonWithoutText(buttonColor)) { () =>
			parentWindow.foreach { window =>
				new EditDatabaseDialog().display(window).foreach { _.foreach { newConfig =>
					dataManager.addNewDatabase(newConfig)
				} }
			}
		}
	}
	
	
	// INITIAL CODE	-------------------------
	
	selectionDD.selectOne(dataManager.value)
	
	// When user selects a different DB from the DD, informs the data manager (also updates selection based on
	// data manager changes)
	selectionDD.addValueListener { _.newValue.foreach { dataManager.value = _ } }
	dataManager.addContentListener { e => selectionDD.content = e.newValue }
	dataManager.addValueListener { e => selectionDD.selectOne(e.newValue) }
	
	
	// IMPLEMENTED	-------------------------
	
	override protected def wrapped = view
	
	override def contentPointer = dataManager.contentPointer
	
	override def valuePointer = dataManager.valuePointer
}
