package dbd.client.vc

import utopia.reflection.localization.LocalString._
import dbd.client.controller.Icons
import dbd.core.model.AttributeType.{DoubleType, IntType, ShortStringType}
import dbd.core.model.{Attribute, AttributeType}
import utopia.genesis.image.Image
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.label.{ImageLabel, TextLabel}
import utopia.reflection.container.stack.StackLayout.Center
import utopia.reflection.container.stack.segmented.SegmentedGroup
import utopia.reflection.container.swing.SegmentedRow
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

/**
 * Displays attribute's information on a row
 * @author Mikko Hilpinen
 * @since 11.1.2020, v0.1
 */
class AttributeRowVC(private val group: SegmentedGroup, initialAttribute: Attribute)(implicit baseCB: ComponentContextBuilder)
	extends StackableAwtComponentWrapperWrapper with Refreshable[Attribute]
{
	// ATTRIBUTES	----------------------
	
	private var _content = initialAttribute
	
	private implicit val baseContext: ComponentContext = baseCB.result
	
	private val imageLabel = ImageLabel.contextual(iconForType(initialAttribute.configuration.dataType))
	private val attNameLabel = TextLabel.contextual(initialAttribute.configuration.name.noLanguageLocalizationSkipped)
	
	private val row = SegmentedRow.partOfGroupWithItems(group, Vector(imageLabel, attNameLabel), layout = Center)
	
	
	// INITIAL CODE	----------------------
	
	updateFont()
	
	
	// IMPLEMENTED	----------------------
	
	override protected def wrapped = row
	
	override def content_=(newContent: Attribute) =
	{
		_content = newContent
		imageLabel.image = iconForType(newContent.configuration.dataType)
		attNameLabel.text = newContent.configuration.name.noLanguageLocalizationSkipped
		updateFont()
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
	
	private def iconForType(attributeType: AttributeType) =
	{
		val icon = attributeType match
		{
			case ShortStringType => Icons("text.png")
			case IntType => Icons("numbers.png")
			case DoubleType => Icons("decimal.png")
			case _ => Image.empty // TODO: Add support for other types
		}
		icon.withAlpha(0.55)
	}
}
