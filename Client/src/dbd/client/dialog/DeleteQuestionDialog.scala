package dbd.client.dialog

import utopia.reflection.localization.LocalString._
import dbd.client.controller.Icons
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.MultiLineTextView
import utopia.reflection.localization.{LocalizedString, Localizer}
import utopia.reflection.util.{ComponentContext, Screen}

object DeleteQuestionDialog
{
	private implicit val language: String = "en"
	
	// TODO: Inform which classes are affected by this change (on attribute mapped links and on class, linked classes
	//  and sub-classes and classes linked to those)
	
	/**
	 * Creates a personalized delete question dialog
	 * @param deletedItemType Localized name of the deleted item's type
	 * @param deletedItemName Localized name of the deleted item itself
	 * @param baseContext Component creation context (implicit)
	 * @param colorScheme Color scheme (implicit)
	 * @param localizer Localizer used (implicit)
	 * @return A new dialog
	 */
	def personalized(deletedItemType: LocalizedString, deletedItemName: LocalizedString)
					(implicit baseContext: ComponentContext, colorScheme: ColorScheme, localizer: Localizer) =
		new DeleteQuestionDialog("Delete %s '%s'".autoLocalized.interpolate(deletedItemType, deletedItemName),
			"Are you sure you wish to permanently delete %s '%s'?".autoLocalized.interpolate(deletedItemType, deletedItemName))
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete an attribute
	 * @param attributeName Name of the attribute to delete
	 * @param baseContext Component creation context (implicit)
	 * @param localizer Localizer used (implicit)
	 * @param colorScheme Color scheme (implicit)
	 * @return A new dialog
	 */
	def forAttribute(attributeName: String)
			 (implicit baseContext: ComponentContext, localizer: Localizer, colorScheme: ColorScheme) =
		DeleteQuestionDialog.personalized("attribute", attributeName.noLanguageLocalizationSkipped)
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete a class
	 * @param className Name of the class to delete
	 * @param baseContext Component creation context (implicit)
	 * @param localizer Localizer used (implicit)
	 * @param colorScheme Color scheme (implicit)
	 * @return A new dialog
	 */
	def forClass(className: String)
				(implicit baseContext: ComponentContext, localizer: Localizer, colorScheme: ColorScheme) =
		DeleteQuestionDialog.personalized("class", className.noLanguageLocalizationSkipped)
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete a link
	 * @param linkName Name of the link to delete
	 * @param baseContext Component creation context (implicit)
	 * @param localizer Localizer used (implicit)
	 * @param colorScheme Color scheme (implicit)
	 * @return A new dialog
	 */
	def forLink(linkName: String)
				(implicit baseContext: ComponentContext, localizer: Localizer, colorScheme: ColorScheme) =
		DeleteQuestionDialog.personalized("link", linkName.noLanguageLocalizationSkipped)
}

/**
 * Used for checking whether the user really wants to delete an item
 * @author Mikko Hilpinen
 * @since 13.1.2020, v0.1
 */
class DeleteQuestionDialog(override val title: LocalizedString, question: LocalizedString)
						  (implicit baseContext: ComponentContext, colorScheme: ColorScheme)
	extends InteractionDialog[Boolean]
{
	// IMPLEMENTED	------------------------
	
	override protected def buttonData = Vector(new DialogButtonInfo[Boolean]("Yes", Icons.delete,
		colorScheme.error, () => Some(true) -> true), DialogButtonInfo.cancel("No"))
	
	override protected def dialogContent = MultiLineTextView.contextual(question, Screen.size.width / 3)
	
	override protected def defaultResult = false
}
