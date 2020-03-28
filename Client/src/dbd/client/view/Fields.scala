package dbd.client.view

import dbd.client.controller.Icons
import dbd.client.model.Icon
import dbd.core.util.ThreadPool
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.genesis.color.Color
import utopia.reflection.color.ComponentColor
import utopia.reflection.component.Refreshable
import utopia.reflection.component.swing.{DropDown, SearchFrom}
import utopia.reflection.component.swing.label.{ImageAndTextLabel, ItemLabel, TextLabel}
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.util.{ComponentContext, ComponentContextBuilder}

import scala.concurrent.ExecutionContext

/**
  * Used for creating view components
  * @author Mikko Hilpinen
  * @since 21.3.2020, v1
  */
object Fields
{
	private implicit def exc: ExecutionContext = ThreadPool.executionContext
	
	/**
	  * Creates a search from field that displays icons and text
	  * @param noResultsText Text displayed when no results can be found
	  * @param selectionPrompt Prompt displayed when no selection has been made
	  * @param displayFunction Function for converting items to displayable strings
	  * @param background Component background color
	  * @param items Items to select from
	  * @param itemToIcon Function for converting items to icons
	  * @param baseCB Component creation context builder
	  * @tparam A Type of selected item
	  * @return A new field
	  */
	def searchFromWithIcons[A](noResultsText: LocalizedString, selectionPrompt: LocalizedString,
							   displayFunction: DisplayFunction[A] = DisplayFunction.raw,
							   background: ComponentColor = Color.white, items: Vector[A] = Vector())(itemToIcon: A => Icon)
							  (implicit baseCB: ComponentContextBuilder) =
	{
		val textColor = background.defaultTextColor
		implicit val context: ComponentContext = baseCB.withTextColor(textColor).result
		customSearchFrom[A, ImageAndTextLabel[A]](noResultsText, selectionPrompt, displayFunction, background, items) {
			item => ImageAndTextLabel.contextual(item, displayFunction) { item =>
				itemToIcon(item).asImageWithColor(background.defaultTextColor) } }
	}
	
	/**
	  * Creates a search from field
	  * @param noResultsText Text displayed when no results can be found
	  * @param selectionPrompt Prompt displayed when no selection has been made
	  * @param displayFunction Function for converting items to displayable strings
	  * @param background Component background color
	  * @param items Items to select from
	  * @param baseCB Component creation context builder
	  * @tparam A Type of selected item
	  * @return A new field
	  */
	def searchFrom[A](noResultsText: LocalizedString, selectionPrompt: LocalizedString,
					  displayFunction: DisplayFunction[A] = DisplayFunction.raw,
					  background: ComponentColor = Color.white, items: Vector[A] = Vector())(implicit baseCB: ComponentContextBuilder) =
	{
		val textColor = background.defaultTextColor
		implicit val context: ComponentContext = baseCB.withTextColor(textColor).result
		customSearchFrom[A, ItemLabel[A]](noResultsText, selectionPrompt, displayFunction, background, items) {
			item => ItemLabel.contextual(item, displayFunction) }
	}
	
	/**
	  * Creates a drop down field
	  * @param noResultsText Text displayed when no results are available
	  * @param selectionPrompt Text prompting the user to select an item
	  * @param displayFunction Function for converting items to text (default = toString)
	  * @param background Component background (default = white)
	  * @param items Items to select from (default = empty vector)
	  * @param baseCB Component creation context builder
	  * @tparam A Type of selected item
	  * @return A new drop down field
	  */
	def dropDown[A](noResultsText: LocalizedString, selectionPrompt: LocalizedString,
					displayFunction: DisplayFunction[A] = DisplayFunction.raw, background: ComponentColor = Color.white,
					items: Vector[A] = Vector())
				   (implicit baseCB: ComponentContextBuilder) =
	{
		dropDownWithPointer[A](new PointerWithEvents(items), noResultsText, selectionPrompt, displayFunction, background)
	}
	
	/**
	  * Creates a drop down field
	  * @param contentPointer Pointer used for holding all drop down selection options
	  * @param noResultsText Text displayed when no results are available
	  * @param selectionPrompt Text prompting the user to select an item
	  * @param displayFunction Function for converting items to text (default = toString)
	  * @param background Component background (default = white)
	  * @param checkEquals A function for checking equality between items (default = _ == _)
	  * @param baseCB Component creation context builder
	  * @tparam A Type of selected item
	  * @return A new drop down field
	  */
	def dropDownWithPointer[A](contentPointer: PointerWithEvents[Vector[A]], noResultsText: LocalizedString,
							   selectionPrompt: LocalizedString, displayFunction: DisplayFunction[A] = DisplayFunction.raw,
							   background: ComponentColor = Color.white, checkEquals: (A, A) => Boolean = (a: A, b: A) => a == b)
							  (implicit baseCB: ComponentContextBuilder) =
	{
		val textColor = background.defaultTextColor
		implicit val context: ComponentContext = baseCB.withTextColor(textColor).withBorderWidth(1).result
		val dd = DropDown.contextualWithTextOnly[A](TextLabel.contextual(noResultsText, isHint = true).framed(context.insets),
			Icons.expandMore.asImageWithColor(textColor), selectionPrompt, displayFunction,
			background.defaultTextColor, contentPointer = contentPointer, shouldDisplayPopUpOnFocusGain = false, equalsCheck = checkEquals)
		dd.background = background
		dd
	}
	
	private def customSearchFrom[A, C <: AwtStackable with Refreshable[A]]
	(noResultsText: LocalizedString, selectionPrompt: LocalizedString,
	 displayFunction: DisplayFunction[A] = DisplayFunction.raw,
	 background: ComponentColor = Color.white, items: Vector[A] = Vector())(itemToDisplay: A => C)
	(implicit context: ComponentContext) =
	{
		val searchPointer = new PointerWithEvents[Option[String]](None)
		val field = SearchFrom.contextual[A, C](
			SearchFrom.noResultsLabel(noResultsText, searchPointer).framed(context.insets, background),
			selectionPrompt, searchIcon = Some(Icons.search.asImageWithColor(background.defaultTextColor)),
			searchFieldPointer = searchPointer, shouldDisplayPopUpOnFocusGain = false)(
			itemToDisplay) { displayFunction(_).string }
		field.background = background
		field.content = items
		field
	}
}
