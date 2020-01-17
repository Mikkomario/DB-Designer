package dbd.client.vc

import utopia.flow.util.CollectionExtensions._
import dbd.client.controller.Icons
import dbd.client.dialog.EditClassDialog
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.Fonts
import dbd.core.model.existing.{Attribute, Class}
import dbd.core.model.partial.{NewAttribute, NewAttributeConfiguration, NewClassInfo}
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
class ClassVC(initialClass: Class, isInitiallyExpanded: Boolean = false)(onAttributeAdded: (Int, NewAttribute) => Unit)
			 (onAttributeEdited: (Attribute, NewAttributeConfiguration) => Unit)(onAttributeDeleted: Attribute => Unit)
			 (onClassEdited: (Class, NewClassInfo) => Unit)(onClassExpandChanged: (Class, Boolean) => Unit)
			 (implicit baseCB: ComponentContextBuilder, fonts: Fonts, margins: Margins, colorScheme: ColorScheme,
			  defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[(Class, Boolean)]
{
	// ATTRIBUTES	------------------------
	
	private implicit val headerContext: ComponentContext = baseCB.copy(textColor = Color.white).result
	
	private val headerButtonColor = colorScheme.secondary.dark
	private val expandButton = new ImageCheckBox(Icons.expandMore.forButtonWithoutText(headerButtonColor),
		Icons.expandLess.forButtonWithoutText(headerButtonColor), isInitiallyExpanded)
	
	private val classNameLabel = ItemLabel.contextual(initialClass,
		DisplayFunction.noLocalization[Class] { _.info.name })(headerContext)
	
	private val header = Stack.buildRowWithContext(layout = Center) { headerRow =>
		headerRow += expandButton
		headerRow += classNameLabel
		headerRow += ImageButton.contextual(Icons.edit.forButtonWithoutText(headerButtonColor)) { () =>
			parentWindow.foreach { window =>
				val classToEdit = displayedClass
				new EditClassDialog(Some(classToEdit.info)).display(window).foreach { _.foreach { editedInfo =>
					onClassEdited(classToEdit, editedInfo)
					/*
					ConnectionPool.tryWith { implicit connection =>
						database.Class(classToEdit.id).info.update(editedInfo)
					} match
					{
						case Success(info) =>
							if (info.classId == displayedClass.id)
								displayedClass = displayedClass.update(info)
						case Failure(error) =>
							Log(error, "Failed to edit class info")
					}*/
				} }
			}
		}
	}.framed(margins.small.downscaling x margins.small.any, colorScheme.primary)
	
	private val attributeSection = new AttributesVC(initialClass.id, orderedAttributes(initialClass))(
		onAttributeAdded)(onAttributeEdited)(onAttributeDeleted)
	
	private val view = Stack.columnWithItems(Vector(header, attributeSection), margin = 0.fixed)
	
	
	// INITIAL CODE	------------------------
	
	attributeSection.isVisible = isInitiallyExpanded
	expandButton.addValueListener { e => onClassExpandChanged(displayedClass, e.newValue) }
	classNameLabel.addMouseButtonListener(MouseButtonStateListener.onButtonPressedInside(MouseButton.Left,
		classNameLabel.bounds, _ => { expandButton.value = true; Some(ConsumeEvent("Class expanded")) }))
	
	
	// COMPUTED	----------------------------
	
	def displayedClass = classNameLabel.content
	def displayedClass_=(newClass: Class) = content = newClass -> isExpanded
	
	def isExpanded = expandButton.value
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: (Class, Boolean)) =
	{
		val (newClass, expand) = newContent
		
		// Content may be shrinked or expanded
		expandButton.value = expand
		attributeSection.isVisible = expand
		classNameLabel.content = newClass
		attributeSection.content = newClass.id -> orderedAttributes(newClass)
	}
	
	override def content = classNameLabel.content -> expandButton.value
	
	
	// OTHER	---------------------------
	
	private def orderedAttributes(c: Class) = c.attributes.sortedWith(Ordering.by { !_.isSearchKey },
		Ordering.by { _.isOptional }, Ordering.by { _.name })
}
