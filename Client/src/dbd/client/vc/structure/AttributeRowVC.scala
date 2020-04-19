package dbd.client.vc.structure

import dbd.client.controller.{ClassDisplayManager, Icons}
import dbd.client.dialog.{DeleteQuestionDialog, EditAttributeDialog, InfoDialog}
import dbd.core.model.enumeration.AttributeType
import dbd.core.model.enumeration.AttributeType._
import dbd.core.model.existing.Attribute
import utopia.genesis.color.Color
import utopia.genesis.image.Image
import utopia.genesis.shape.shape2D.Line
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.Refreshable
import utopia.reflection.component.drawing.template.{CustomDrawer, DrawLevel}
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageButton
import utopia.reflection.component.swing.label.{ImageLabel, TextLabel}
import utopia.reflection.container.stack.StackLayout.Center
import utopia.reflection.container.stack.segmented.SegmentedGroup
import utopia.reflection.container.swing.SegmentedRow
import utopia.reflection.localization.LocalString._
import utopia.reflection.localization.Localizer
import utopia.reflection.shape.LengthExtensions._
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
 * Displays attribute's information on a row
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class AttributeRowVC(private val group: SegmentedGroup, initialAttribute: Attribute, classManager: ClassDisplayManager,
					 parentBackground: Color)
					(implicit baseCB: ComponentContextBuilder, margins: Margins, colorScheme: ColorScheme,
					 defaultLanguageCode: String, localizer: Localizer, exc: ExecutionContext)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Attribute]
{
	// ATTRIBUTES	----------------------
	
	private var _content = initialAttribute
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private val imageLabel = ImageLabel.contextual(iconForType(initialAttribute.configuration.dataType))
	private val attNameLabel = TextLabel.contextual(initialAttribute.configuration.name.noLanguageLocalizationSkipped)(
		baseCB.mapInsets { _.mapRight { _.expanding } }.result)
	private val buttonColor = colorScheme.secondary.forBackground(parentBackground)
	private val editAttributeButton = ImageButton.contextual(Icons.edit.forButtonWithoutText(buttonColor)) { () =>
		parentWindow.foreach { window =>
			val attributeToEdit = content
			new EditAttributeDialog(Some(attributeToEdit)).display(window).foreach { _.foreach { edited =>
				classManager.editAttribute(attributeToEdit, edited)
			} }
		}
	}
	// TODO: WET WET
	private val deleteAttributeButton = ImageButton.contextual(Icons.close.forButtonWithoutText(buttonColor)) { () =>
		parentWindow.foreach { window =>
			val attributeToDelete = content
			// Will not delete the attribute if it's used in an owned link
			if (classManager.attributeIsUsedInOwnedLinks(attributeToDelete.id))
				new InfoDialog("Warning",
					"Cannot delete %s because it's used in parent-child links.\nRemove or edit the links first and then try again."
						.autoLocalized.interpolated(Vector(attributeToDelete.name)),
					Some(Icons.warning.fullSize.asImageWithColor(colorScheme.error))).display(window)
			else
				DeleteQuestionDialog.forAttribute(attributeToDelete.name,
					classManager.classesAffectedByAttributeDeletion(attributeToDelete)).display(window).foreach {
					if (_) classManager.deleteAttribute(attributeToDelete) }
		}
	}
	
	private val row = SegmentedRow.partOfGroupWithItems(group, Vector(imageLabel, attNameLabel, editAttributeButton,
		deleteAttributeButton), margin = margins.small.downscaling, layout = Center)
	
	private lazy val searchKeyDrawer = CustomDrawer(DrawLevel.Foreground) { (drawer, bounds) =>
		drawer.withEdgeColor(Color.black.withAlpha(0.55)).withStroke(margins.verySmall.toInt)
			.draw(Line(bounds.bottomLeft, bounds.bottomRight)) }
	
	
	// INITIAL CODE	----------------------
	
	updateFont()
	if (initialAttribute.isSearchKey)
		attNameLabel.addCustomDrawer(searchKeyDrawer)
	
	
	// IMPLEMENTED	----------------------
	
	override protected def wrapped = row
	
	override def content_=(newContent: Attribute) =
	{
		val oldContentWasSearchKey = content.isSearchKey
		
		_content = newContent
		imageLabel.image = iconForType(newContent.configuration.dataType)
		attNameLabel.text = newContent.configuration.name.noLanguageLocalizationSkipped
		updateFont()
		
		// A custom drawer is used for search keys
		if (newContent.isSearchKey != oldContentWasSearchKey)
		{
			if (newContent.isSearchKey)
				attNameLabel.addCustomDrawer(searchKeyDrawer)
			else
				attNameLabel.removeCustomDrawer(searchKeyDrawer)
		}
	}
	
	override def content = _content
	
	
	// OTHER	--------------------------
	
	/**
	 * Removes this component from the segmented row group it's part of
	 */
	def unregister() = group.remove(row)
	
	private def updateFont() = attNameLabel.font =
	{
		if (content.configuration.isOptional)
			attNameLabel.font.plain else attNameLabel.font.bold
	}
	
	// TODO: Replace with Icon.forAttributeType(...)
	private def iconForType(attributeType: AttributeType) =
	{
		val icon = attributeType match
		{
			case ShortStringType => Icons.text.black
			case IntType => Icons.numbers.black
			case DoubleType => Icons.decimalNumber.black
			case BooleanType => Icons.checkBox.black
			case InstantType => Icons.time.black
			case _ => Image.empty // TODO: Add support for other types
		}
		icon.withAlpha(0.55)
	}
}
