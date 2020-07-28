package dbd.client.vc

import utopia.reflection.shape.LengthExtensions._
import dbd.client.vc.MainView.ClassStructure
import dbd.client.vc.structure.ClassesVC
import dbd.client.vc.version.ReleasesVC
import utopia.genesis.shape.Axis.X
import utopia.genesis.shape.shape2D.Direction2D
import utopia.reflection.component.template.display.Refreshable
import utopia.reflection.container.swing.layout.multi.Stack.AwtStackable
import utopia.reflection.container.swing.AwtContainerRelated
import utopia.reflection.localization.{DisplayFunction, LocalString}
import utopia.reflection.shape.StackLength
import utopia.genesis.util.Screen
import utopia.reflection.component.swing.input.TabSelection
import utopia.reflection.component.swing.template.StackableAwtComponentWrapperWrapper
import utopia.reflection.container.swing.layout.multi.Stack
import utopia.reflection.container.swing.layout.wrapper.SwitchPanel

/**
  * This VC controls the main view
  * @author Mikko Hilpinen
  * @since 1.2.2020, v0.1
  */
class MainVC extends StackableAwtComponentWrapperWrapper with AwtContainerRelated
{
	import dbd.client.view.DefaultContext._
	
	// ATTRIBUTES	-----------------------
	
	private val headersContext = baseContext.inContextWithBackground(colorScheme.primary.dark)
	
	private val dbVC = new DatabaseSelectionVC()(headersContext)
	
	private val mainTab = headersContext.forTextComponents().mapInsets { _.mapAxis(X) { _.expanding } }.use { implicit txtC =>
		TabSelection.contextualWithBackground[MainView](colorScheme.primary,
			DisplayFunction.localized[MainView] { _.name }, MainView.values)
	}
	
	private val mainContentPanel = new SwitchPanel[AwtStackable with Refreshable[Int]]
	
	private val view = Stack.buildColumn(margin = 0.fixed) { stack =>
		// Adds header that contains the database selection
		stack += dbVC.alignedToSide(Direction2D.Left).framed(
			margins.medium.downscaling x StackLength(margins.small, margins.medium), headersContext.containerBackground)
		// Adds the main content
		stack += mainTab
		stack += mainContentPanel
	}
	
	
	// INITIAL CODE	------------------------
	
	// Whenever database selection changes, informs the main view
	dbVC.addValueListener { e => mainContentPanel.content.foreach { _.content = e.newValue.id } }
	
	mainTab.selectOne(ClassStructure)
	mainContentPanel.set(ClassStructure.constructView(dbVC.selected.id))
	
	// Whenever main tab is switched, reconstructs the main view
	mainTab.addValueListener { _.newValue.foreach { newState =>
		mainContentPanel.set(newState.constructView(dbVC.selected.id)) }
	}
	
	
	// IMPLEMENTED	------------------------
	
	override def component = view.component
	
	override protected def wrapped = view
}

private sealed trait MainView
{
	val name: LocalString
	
	def constructView(currentDatabaseId: Int): AwtStackable with Refreshable[Int]
}

private object MainView
{
	private implicit val language: String = "en"
	
	case object ClassStructure extends MainView
	{
		override val name = "Classes"
		
		override def constructView(currentDatabaseId: Int) = new ClassesVC(Screen.height * 0.7, currentDatabaseId)
	}
	
	case object Releases extends MainView
	{
		override val name = "Releases"
		
		override def constructView(currentDatabaseId: Int) = new ReleasesVC(currentDatabaseId)
	}
	
	val values = Vector(ClassStructure, Releases)
}
