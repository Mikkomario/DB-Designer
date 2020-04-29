package dbd.client.view

import dbd.client.controller.Icons
import dbd.client.model.Icon
import utopia.flow.datastructure.mutable.PointerWithEvents
import utopia.reflection.component.Refreshable
import utopia.reflection.component.context.{ButtonContext, ButtonContextLike}
import utopia.reflection.component.swing.{DropDown, SearchFrom}
import utopia.reflection.component.swing.label.{ImageAndTextLabel, ItemLabel, TextLabel}
import utopia.reflection.container.stack.StackLayout.Leading
import utopia.reflection.container.swing.Stack.AwtStackable
import utopia.reflection.localization.{DisplayFunction, LocalizedString}
import utopia.reflection.shape.LengthExtensions._

/**
  * Used for creating view components
  * @author Mikko Hilpinen
  * @since 21.3.2020, v1
  */
object Fields
{
	import DefaultContext._
	
	/**
	  * Creates a search from field that displays icons and text
	  * @param noResultsText Text displayed when no results can be found
	  * @param selectionPrompt Prompt displayed when no selection has been made
	  * @param displayFunction Function for converting items to displayable strings
	  * @param items Items to select from
	  * @param itemToIcon Function for converting items to icons
	  * @param context Context used for creating this field (determines background etc.)
	  * @tparam A Type of selected item
	  * @return A new field
	  */
	def searchFromWithIcons[A](noResultsText: LocalizedString, selectionPrompt: LocalizedString,
							   displayFunction: DisplayFunction[A] = DisplayFunction.raw,
							   items: Vector[A] = Vector())(itemToIcon: A => Icon)
							  (implicit context: ButtonContextLike) =
	{
		customSearchFrom[A, ImageAndTextLabel[A]](noResultsText, selectionPrompt, displayFunction, items) {
			item => ImageAndTextLabel.contextual(item, displayFunction) { item =>
				itemToIcon(item).singleColorImage } }
	}
	
	/**
	  * Creates a search from field
	  * @param noResultsText Text displayed when no results can be found
	  * @param selectionPrompt Prompt displayed when no selection has been made
	  * @param displayFunction Function for converting items to displayable strings
	  * @param items Items to select from
	  * @param context Component creation context
	  * @tparam A Type of selected item
	  * @return A new field
	  */
	def searchFrom[A](noResultsText: LocalizedString, selectionPrompt: LocalizedString,
					  displayFunction: DisplayFunction[A] = DisplayFunction.raw, items: Vector[A] = Vector())
					 (implicit context: ButtonContextLike) =
	{
		customSearchFrom[A, ItemLabel[A]](noResultsText, selectionPrompt, displayFunction, items) {
			item => ItemLabel.contextual(item, displayFunction) }
	}
	
	/**
	  * Creates a drop down field
	  * @param noResultsText Text displayed when no results are available
	  * @param selectionPrompt Text prompting the user to select an item
	  * @param displayFunction Function for converting items to text (default = toString)
	  * @param items Items to select from (default = empty vector)
	  * @param baseCB Component creation context builder
	  * @tparam A Type of selected item
	  * @return A new drop down field
	  */
	def dropDown[A](noResultsText: LocalizedString, selectionPrompt: LocalizedString,
					displayFunction: DisplayFunction[A] = DisplayFunction.raw, items: Vector[A] = Vector())
				   (implicit baseCB: ButtonContext) =
	{
		dropDownWithPointer[A](new PointerWithEvents(items), noResultsText, selectionPrompt, displayFunction)
	}
	
	/**
	  * Creates a drop down field
	  * @param contentPointer Pointer used for holding all drop down selection options
	  * @param noResultsText Text displayed when no results are available
	  * @param selectionPrompt Text prompting the user to select an item
	  * @param displayFunction Function for converting items to text (default = toString)
	  * @param sameInstanceCheck A function for checking whether two items represent the same instance (default: _ == _)
	  * @param contentIsStateless Whether sameInstanceCheck always checks for exact equality (default = true).
	  * @param context Component creation context
	  * @tparam A Type of selected item
	  * @return A new drop down field
	  */
	def dropDownWithPointer[A](contentPointer: PointerWithEvents[Vector[A]], noResultsText: LocalizedString,
							   selectionPrompt: LocalizedString, displayFunction: DisplayFunction[A] = DisplayFunction.raw,
							   sameInstanceCheck: (A, A) => Boolean = (a: A, b: A) => a == b,
							   contentIsStateless: Boolean = true)(implicit context: ButtonContext) =
	{
		val noResultsView = TextLabel.contextual(noResultsText, isHint = true).framed(context.margins.small.any, context.buttonColor)
		val dd = DropDown.contextualWithTextOnly[A](noResultsView, Icons.expandMore.singleColorImage, selectionPrompt,
			displayFunction, contentPointer = contentPointer, shouldDisplayPopUpOnFocusGain = false,
			sameInstanceCheck = sameInstanceCheck, contentIsStateless = contentIsStateless)(
			context.withBorderWidth(1), exc)
		dd
	}
	
	private def customSearchFrom[A, C <: AwtStackable with Refreshable[A]]
	(noResultsText: LocalizedString, selectionPrompt: LocalizedString,
	 displayFunction: DisplayFunction[A] = DisplayFunction.raw, items: Vector[A] = Vector())(itemToDisplay: A => C)
	(implicit context: ButtonContextLike) =
	{
		val background = context.buttonColor
		val searchPointer = new PointerWithEvents[Option[String]](None)
		val noResultsView = SearchFrom.noResultsLabel(noResultsText, searchPointer).framed(context.margins.small.any, background)
		val field = SearchFrom.contextual[A, C](noResultsView, selectionPrompt, standardInputWidth.any, Leading,
			searchIcon = Some(Icons.search.singleColorImage), searchFieldPointer = searchPointer)(itemToDisplay) { displayFunction(_).string }
		field.content = items
		field
	}
}
