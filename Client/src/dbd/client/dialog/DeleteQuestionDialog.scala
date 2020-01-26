package dbd.client.dialog

import utopia.reflection.localization.LocalString._
import dbd.client.controller.Icons
import dbd.core.model.template.ClassLike
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.MultiLineTextView
import utopia.reflection.localization.{LocalizedString, Localizer}
import utopia.reflection.util.{ComponentContext, Screen}

object DeleteQuestionDialog
{
	private implicit val language: String = "en"
	
	/**
	 * Creates a personalized delete question dialog
	 * @param deletedItemType Localized name of the deleted item's type
	 * @param deletedItemName Localized name of the deleted item itself
	 * @param baseContext Component creation context (implicit)
	 * @param colorScheme Color scheme (implicit)
	 * @param localizer Localizer used (implicit)
	 * @return A new dialog
	 */
	def personalized(deletedItemType: LocalizedString, deletedItemName: LocalizedString,
					 affectedTypeName: Option[LocalizedString] = None, affectedItems: Vector[String] = Vector())
					(implicit baseContext: ComponentContext, colorScheme: ColorScheme, localizer: Localizer) =
	{
		val messageBuilder = new StringBuilder
		messageBuilder ++= "Are you sure you wish to permanently delete %s '%s'?"
		if (affectedItems.nonEmpty)
		{
			if (affectedTypeName.isDefined)
				messageBuilder ++= "\nThis also affects the following %s:"
			else
				messageBuilder ++= "\nThis also affects:"
			
			messageBuilder ++= "\n\t- %s" * affectedItems.size
		}
		val question = messageBuilder.result().autoLocalized.interpolate(Vector(deletedItemType, deletedItemName) ++
			affectedTypeName ++ affectedItems)
		new DeleteQuestionDialog("Delete %s '%s'".autoLocalized.interpolate(deletedItemType, deletedItemName), question)
	}
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete an attribute
	 * @param attributeName Name of the attribute to delete
	 * @param baseContext Component creation context (implicit)
	 * @param localizer Localizer used (implicit)
	 * @param colorScheme Color scheme (implicit)
	 * @return A new dialog
	 */
	def forAttribute(attributeName: String, affectedClasses: Vector[ClassLike[_, _, _]] = Vector())
			 (implicit baseContext: ComponentContext, localizer: Localizer, colorScheme: ColorScheme) =
		DeleteQuestionDialog.personalized("attribute", attributeName.noLanguageLocalizationSkipped,
			Some("classes"), affectedClasses.map { _.name })
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete a class
	 * @param className Name of the class to delete
	 * @param baseContext Component creation context (implicit)
	 * @param localizer Localizer used (implicit)
	 * @param colorScheme Color scheme (implicit)
	 * @return A new dialog
	 */
	def forClass(className: String, affectedClasses: Vector[ClassLike[_, _, _]] = Vector())
				(implicit baseContext: ComponentContext, localizer: Localizer, colorScheme: ColorScheme) =
		DeleteQuestionDialog.personalized("class", className.noLanguageLocalizationSkipped,
			Some("classes"), affectedClasses.map { _.name })
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete a link
	 * @param linkName Name of the link to delete
	 * @param baseContext Component creation context (implicit)
	 * @param localizer Localizer used (implicit)
	 * @param colorScheme Color scheme (implicit)
	 * @return A new dialog
	 */
	def forLink(linkName: String, otherLinkClass: Option[ClassLike[_, _, _]] = None)
				(implicit baseContext: ComponentContext, localizer: Localizer, colorScheme: ColorScheme) =
		DeleteQuestionDialog.personalized("link", linkName.noLanguageLocalizationSkipped,
			Some("class"), otherLinkClass.map { _.name }.toVector)
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
