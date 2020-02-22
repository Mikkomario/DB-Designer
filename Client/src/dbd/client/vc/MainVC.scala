package dbd.client.vc

import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.Fonts
import dbd.client.vc.MainView.ClassStructure
import dbd.client.vc.structure.ClassesVC
import dbd.client.vc.version.ReleasesVC
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.{StackableAwtComponentWrapperWrapper, TabSelection}
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.{AwtContainerRelated, Stack, SwitchPanel}
import utopia.reflection.localization.{DisplayFunction, LocalString, Localizer}
import utopia.reflection.shape.{Margins, StackLength}
import utopia.reflection.util.{ComponentContextBuilder, Screen}

import scala.concurrent.ExecutionContext

/**
  * This VC controls the main view
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class MainVC(implicit baseCB: ComponentContextBuilder, exc: ExecutionContext, localizer: Localizer, margins: Margins,
			 fonts: Fonts, colorScheme: ColorScheme)
	extends StackableAwtComponentWrapperWrapper with AwtContainerRelated
{
	// ATTRIBUTES	-----------------------
	
	private val dbVC = new DatabaseSelectionVC(colorScheme.primary.dark.defaultTextColor)
	
	private val mainTab = TabSelection.contextual[MainView](DisplayFunction.localized[MainView] { _.name }, MainView.values,
		margins.small)(baseCB.withColors(colorScheme.primary).withHighlightColor(colorScheme.secondary.light).result)
	private val mainContentPanel = new SwitchPanel[AwtStackable with Refreshable[Int]]
	
	private val view = Stack.buildColumn(margin = 0.fixed) { stack =>
		// Adds header that contains the database selection
		stack += dbVC.alignedToSide(Direction2D.Left, useLowPriorityLength = true).framed(
			margins.medium.downscaling x StackLength(margins.small, margins.medium), colorScheme.primary.dark)
		// Adds the main content
		stack += mainTab
		stack += mainContentPanel
	}
	
	
	// INITIAL CODE	------------------------
	
	// Whenever database selection changes, informs the main view
	dbVC.addValueListener { e => mainContentPanel.content.foreach { _.content = e.newValue.id } }
	
	// Whenever main tab is switched, reconstructs the main view
	mainTab.addValueListener { _.newValue.foreach { newState =>
		mainContentPanel.set(newState.constructView(dbVC.selected.id)) }
	}
	
	// Starts with class VC open
	mainTab.selectOne(ClassStructure)
	
	
	// IMPLEMENTED	------------------------
	
	override def component = view.component
	
	override protected def wrapped = view
}

private sealed trait MainView
{
	val name: LocalString
	
	def constructView(currentDatabaseId: Int)
					 (implicit margins: Margins, baseCB: ComponentContextBuilder, colorScheme: ColorScheme,
					  localizer: Localizer, exc: ExecutionContext): AwtStackable with Refreshable[Int]
}

private object MainView
{
	private implicit val language: String = "en"
	
	case object ClassStructure extends MainView
	{
		override val name = "Classes"
		
		override def constructView(currentDatabaseId: Int)
								  (implicit margins: Margins, baseCB: ComponentContextBuilder, colorScheme: ColorScheme,
								   localizer: Localizer, exc: ExecutionContext) =
			new ClassesVC(Screen.height * 0.7, currentDatabaseId)
	}
	
	case object Releases extends MainView
	{
		override val name = "Releases"
		
		override def constructView(currentDatabaseId: Int)
								  (implicit margins: Margins, baseCB: ComponentContextBuilder, colorScheme: ColorScheme,
								   localizer: Localizer, exc: ExecutionContext) = new ReleasesVC(currentDatabaseId)
	}
	
	val values = Vector(ClassStructure, Releases)
}
