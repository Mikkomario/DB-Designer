package dbd.client.vc

import utopia.reflection.shape.LengthExtensions._
import dbd.client.controller.Icons
import utopia.reflection.localization.LocalString._
import dbd.client.model.ChangedItems
import utopia.reflection.color.ComponentColor
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.StackableAwtComponentWrapperWrapper
import utopia.reflection.component.swing.button.ImageButton
import utopia.reflection.component.swing.label.TextLabel
import utopia.reflection.container.stack.StackLayout.Leading
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.container.swing.{Stack, SwitchPanel}
import utopia.reflection.localization.LocalizedString
import utopia.reflection.shape.Margins
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.collection.immutable.VectorBuilder

/**
  * Displays a list of changed items on a row
  * @author Mikko Hilpinen
  * @since 2.2.2020, v0.1
  */
class ChangeListVC(initialList: ChangedItems, initialIsExpanded: Boolean, initialTitle: LocalizedString,
				   maxItemsPerRow: Int, backgroundUsed: ComponentColor)
				  (implicit baseCB: ComponentContextBuilder, margins: Margins)
	extends StackableAwtComponentWrapperWrapper with Refreshable[(LocalizedString, ChangedItems, Boolean)]
{
	// ATTRIBUTES	-----------------------
	
	private implicit val baseContext: ComponentContext = baseCB.copy(textColor = backgroundUsed.defaultTextColor).result
	
	private val betweenItemsMargin = margins.small.downscaling
	
	private var _content = initialList
	private var _isExpanded = initialIsExpanded
	
	private val titleLabel = TextLabel.contextual(initialTitle)
	private val rowsSwitch = new SwitchPanel[AwtStackable]
	private val view = Stack.buildRowWithContext(Leading) { stack =>
		stack += titleLabel
		stack += rowsSwitch
	}
	
	
	// INITIAL CODE	----------------------
	
	// Sets initial view
	updateView()
	
	
	// COMPUTED	---------------------------
	
	private def allTextAndWidths = _content.classes.map { _.name -> 1 } ++
		_content.attributes.map { case (c, a) => s"${c.name}.${a.name}" -> 2 } ++
		_content.links.map { case (a, b) => s"${a.name} <-> ${b.name}" -> 2 }
	
	
	// IMPLEMENTED	-----------------------
	
	override protected def wrapped = view
	
	override def content_=(newContent: (LocalizedString, ChangedItems, Boolean)) =
	{
		val (newTitle, newItems, newExpand) = newContent
		
		if (newTitle != titleLabel.text || newExpand != _isExpanded || newItems != _content)
		{
			_content = newItems
			_isExpanded = newExpand
			titleLabel.text = newTitle
			updateView()
		}
	}
	
	override def content = (titleLabel.text, _content, _isExpanded)
	
	
	// OTHER	---------------------------
	
	private def updateView(): Unit =
	{
		val newView = if (_isExpanded) makeExpandedRows() else makeSingleRow()
		rowsSwitch.set(newView)
	}
	
	private def makeSingleRow() =
	{
		val allTexts = allTextAndWidths
		
		var currentWidth = 0
		// Takes as many items as can be fitted into a row (leaves 1 spot for the "...")
		val displayedTexts = allTexts.takeWhile { case (_, width) =>
			currentWidth += width
			currentWidth < maxItemsPerRow
		}.map { _._1 }
		
		// May replace the "..." with the last item in the list
		val finalTexts = if (displayedTexts.size == allTexts.size - 1 && allTexts.last._2 < 2)
			displayedTexts :+ allTexts.last._1 else displayedTexts
		val canBeExtended = finalTexts.size < allTexts.size
		
		// Creates the row
		val textLabels = finalTexts.map { t => TextLabel.contextual(t.noLanguageLocalizationSkipped) }
		val finalItems =
		{
			if (canBeExtended)
			{
				val expandButton = ImageButton.contextual(Icons.more.forButtonWithoutText(baseContext.textColor)) { () =>
					_isExpanded = true
					updateView()
				}.alignedToCenter
				textLabels :+ expandButton
			}
			else
				textLabels
		}
		
		Stack.rowWithItems(finalItems, margin = betweenItemsMargin)
	}
	
	private def makeExpandedRows() =
	{
		val allTexts = allTextAndWidths
		val rowsBuilder = new VectorBuilder[Vector[String]]
		var currentRowBuilder = new VectorBuilder[String]
		var currentRowWidth = 0
		
		allTexts.foreach { case (text, width) =>
			// Cuts rows when they are full
			if (currentRowWidth + width > maxItemsPerRow)
			{
				rowsBuilder += currentRowBuilder.result
				currentRowBuilder = new VectorBuilder[String]
				currentRowWidth = 0
			}
			currentRowBuilder += text
			currentRowWidth += width
		}
		// Adds final row
		rowsBuilder += currentRowBuilder.result()
		
		// Creates text labels and stacks
		val rowStacks = rowsBuilder.result().map { rowTexts => Stack.rowWithItems(rowTexts.map {
			t => TextLabel.contextual(t.noLanguageLocalizationSkipped) }, margin = betweenItemsMargin) }
		
		// Returns final stack
		Stack.columnWithItems(rowStacks, margin = margins.small.downscaling)
	}
}
