package dbd.client.vc

import utopia.flow.util.CollectionExtensions._
import dbd.core.database
import dbd.client.controller.{ConnectionPool, Icons}
import dbd.client.dialog.EditClassDialog
import utopia.reflection.shape.LengthExtensions._
import dbd.client.model.Fonts
import dbd.core.model.existing.{Attribute, Class}
import dbd.core.model.partial.{NewAttribute, NewAttributeConfiguration}
import dbd.core.util.Log
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
import utopia.vault.database.Connection

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Displays interactive UI for a class
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class ClassVC(initialClass: Class)
			 (implicit baseCB: ComponentContextBuilder, fonts: Fonts, margins: Margins, colorScheme: ColorScheme,
			  defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Class]
{
	// ATTRIBUTES	------------------------
	
	private implicit val headerContext: ComponentContext = baseCB.copy(textColor = Color.white).result
	
	private val headerButtonColor = colorScheme.secondary.dark
	private val expandButton = new ImageCheckBox(Icons.expandMore.forButtonWithoutText(headerButtonColor),
		Icons.expandLess.forButtonWithoutText(headerButtonColor))
	
	private val classNameLabel = ItemLabel.contextual(initialClass,
		DisplayFunction.noLocalization[Class] { _.info.name })(headerContext)
	
	private val header = Stack.buildRowWithContext(layout = Center) { headerRow =>
		headerRow += expandButton
		headerRow += classNameLabel
		headerRow += ImageButton.contextual(Icons.edit.forButtonWithoutText(headerButtonColor)) { () =>
			parentWindow.foreach { window =>
				val classToEdit = content
				new EditClassDialog(Some(classToEdit.info)).display(window).foreach { _.foreach { editedInfo =>
					ConnectionPool.tryWith { implicit connection =>
						database.Class(classToEdit.id).info.update(editedInfo)
					} match
					{
						case Success(info) =>
							if (info.classId == content.id)
								content = content.update(info)
						case Failure(error) =>
							Log(error, "Failed to edit class info")
					}
				} }
			}
		}
	}.framed(margins.small.downscaling x margins.small.any, colorScheme.primary)
	
	private val attributeSection = new AttributesVC(newAttributeAdded)(attributeEdited)(attributeDeleted)
	
	private val view = Stack.columnWithItems(Vector(header, attributeSection), margin = 0.fixed)
	
	
	// INITIAL CODE	------------------------
	
	attributeSection.isVisible = false
	attributeSection.content = orderedAttributes(initialClass)
	expandButton.addValueListener { e => attributeSection.isVisible = e.newValue }
	classNameLabel.addMouseButtonListener(MouseButtonStateListener.onButtonPressedInside(MouseButton.Left,
		classNameLabel.bounds, _ => { expandButton.value = true; Some(ConsumeEvent("Class expanded")) }))
	
	
	// IMPLEMENTED	------------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: Class) =
	{
		// Content is shrinked if class is changed
		if (content.id != newContent.id)
			expandButton.value = false
		classNameLabel.content = newContent
		attributeSection.content = orderedAttributes(newContent)
	}
	
	override def content = classNameLabel.content
	
	
	// OTHER	---------------------------
	
	private def newAttributeAdded(attribute: NewAttribute): Unit =
	{
		// Inserts attribute data to DB, then updates this view
		editAttributes { implicit connection => database.Class(content.id).attributes.insert(attribute) } { _ + _ }
	}
	
	private def attributeEdited(attribute: Attribute, edit: NewAttributeConfiguration): Unit =
	{
		// Updates attribute data to DB, then updates this view
		editAttributes { implicit connection =>
			database.Class(attribute.classId).attribute(attribute.id).configuration.update(edit) } { _.update(_) }
	}
	
	private def attributeDeleted(attribute: Attribute): Unit =
	{
		// Deletes attribute from DB, then from this class
		editAttributes { implicit connection =>
			database.Class(attribute.classId).attribute(attribute.id).markDeleted() } { (c, _) => c - attribute }
	}
	
	private def orderedAttributes(c: Class) = c.attributes.sortedWith(Ordering.by { !_.isSearchKey },
		Ordering.by { _.isOptional }, Ordering.by { _.name })
	
	private def editAttributes[R](databaseAction: Connection => R)(modifyClass: (Class, R) => Class) =
	{
		ConnectionPool.tryWith(databaseAction) match
		{
			// TODO: Might not target right class
			case Success(editResult) => content = modifyClass(content, editResult)
			case Failure(error) => Log(error, s"Failed to modify attributes for class $content")
		}
	}
}
