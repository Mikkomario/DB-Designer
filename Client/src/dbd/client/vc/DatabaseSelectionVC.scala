package dbd.client.vc

import dbd.core.model.enumeration.NamingConvention._
import dbd.client.controller.DatabasesManager
import dbd.core.model.existing.Database
import utopia.reflection.component.input.SelectableWithPointers
import utopia.reflection.component.swing.{DropDown, StackableAwtComponentWrapperWrapper}
import utopia.reflection.localization.{DisplayFunction, Localizer}
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
  * Used for selecting currently modified database and editing database configurations
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class DatabaseSelectionVC(implicit exc: ExecutionContext, baseCB: ComponentContextBuilder, localizer: Localizer)
	extends StackableAwtComponentWrapperWrapper with SelectableWithPointers[Database, Vector[Database]]
{
	// ATTRIBUTES	-------------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	private implicit val lang: String = "en"
	
	private val dataManager = new DatabasesManager()
	
	private val selectionDD = DropDown.contextual("Select Database",
		DisplayFunction.noLocalization[Database] { _.configuration.name.toCapitalized }, dataManager.content)
	
	
	// INITIAL CODE	-------------------------
	
	// When user selects a different DB from the DD, informs the data manager (also updates selection based on
	// data manager changes)
	selectionDD.addValueListener { _.newValue.foreach { dataManager.value = _ } }
	dataManager.addContentListener { e => selectionDD.content = e.newValue }
	dataManager.addValueListener { e => selectionDD.selectOne(e.newValue) }
	
	
	// IMPLEMENTED	-------------------------
	
	// TODO: Add other components
	override protected def wrapped = selectionDD
	
	override def contentPointer = dataManager.contentPointer
	
	override def valuePointer = dataManager.valuePointer
}
