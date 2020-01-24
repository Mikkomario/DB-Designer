package dbd.client.vc

import utopia.flow.util.CollectionExtensions._
import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.{DeleteQuestionDialog, EditClassDialog}
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.{DisplayedClass, Fonts}
import dbd.core.model.existing.Class
import utopia.genesis.color.Color
import utopia.genesis.event.{ConsumeEvent, MouseButton}
import utopia.genesis.handling.MouseButtonStateListener
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.{ImageButton, ImageCheckBox}
import utopia.reflection.component.swing.label.ItemLabel
import utopia.reflection.container.stack.StackLayout.Center
import utopia.reflection.container.swing.Stack
import utopia.reflection.localization.{DisplayFunction, Localizer}
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Displays interactive UI for a class
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class ClassVC(initialClass: DisplayedClass, classManager: ClassDisplayManager)
			 (implicit baseCB: ComponentContextBuilder, fonts: Fonts, margins: Margins, colorScheme: ColorScheme,
			  defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[DisplayedClass]
{
	// ATTRIBUTES	------------------------
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private var _content = initialClass
	
	private val headerButtonColor = colorScheme.secondary.dark
	private val expandButton = new ImageCheckBox(Icons.expandMore.forButtonWithoutText(headerButtonColor),
		Icons.expandLess.forButtonWithoutText(headerButtonColor), initialClass.isExpanded)
	
	private val classNameLabel = ItemLabel.contextual(initialClass.classData,
		DisplayFunction.noLocalization[Class] { _.info.name })(baseCB.copy(textColor = Color.white).result)
	
	private val header = Stack.buildRowWithContext(layout = Center) { headerRow =>
		headerRow += expandButton
		headerRow += classNameLabel
		// Adds edit class button
		headerRow += ImageButton.contextual(Icons.edit.forButtonWithoutText(headerButtonColor)) { () =>
			parentWindow.foreach { window =>
				val classToEdit = displayedClass
				new EditClassDialog(Some(classToEdit.info)).display(window).foreach { _.foreach { editedInfo =>
					classManager.editClass(classToEdit.classData, editedInfo)
				} }
			}
		}
		// Adds delete class button
		// TODO: WET WET
		headerRow += ImageButton.contextual(Icons.close.forButtonWithoutText(headerButtonColor)) { () =>
			parentWindow.foreach { window =>
				val classToDelete = displayedClass
				DeleteQuestionDialog.forClass(classToDelete.name).display(window).foreach {
					if (_) classManager.deleteClass(classToDelete) }
			}
		}
	}.framed(margins.small.downscaling x margins.small.any, colorScheme.primary)
	
	private val attributeSection = new AttributesVC(initialClass.classId, orderedAttributes(initialClass.classData), classManager)
	private val linksSection = new LinksVC(initialClass, classManager)
	private val subClassSection = new SubClassesVC(initialClass, classManager)
	private val classContentView = Stack.buildColumnWithContext() { stack =>
		stack += attributeSection
		stack += linksSection
		stack += subClassSection
	}.framed(margins.medium.downscaling.square, colorScheme.gray.dark)
	
	private val view = Stack.columnWithItems(Vector(header, classContentView), margin = 0.fixed)
	
	
	// INITIAL CODE	------------------------
	
	classContentView.isVisible = initialClass.isExpanded
	expandButton.addValueListener { e => classManager.changeClassExpand(displayedClass.classId, e.newValue) }
	classNameLabel.addMouseButtonListener(MouseButtonStateListener.onButtonPressedInside(MouseButton.Left,
		classNameLabel.bounds, _ => { expandButton.value = true; Some(ConsumeEvent("Class expanded")) }))
	
	
	// COMPUTED	----------------------------
	
	def displayedClass = _content
	
	def isExpanded = expandButton.value
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: DisplayedClass) =
	{
		// Content may be shrinked or expanded
		_content = newContent
		expandButton.value = newContent.isExpanded
		classContentView.isVisible = newContent.isExpanded
		classNameLabel.content = newContent.classData
		attributeSection.content = newContent.classId -> orderedAttributes(newContent.classData)
		linksSection.content = newContent
		subClassSection.content = newContent
	}
	
	override def content = _content
	
	
	// OTHER	---------------------------
	
	private def orderedAttributes(c: Class) = c.attributes.sortedWith(Ordering.by { !_.isSearchKey },
		Ordering.by { _.isOptional }, Ordering.by { _.name })
}
