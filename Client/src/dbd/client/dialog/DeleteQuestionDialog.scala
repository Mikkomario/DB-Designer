package dbd.client.dialog

import utopia.reflection.localization.LocalString._
import dbd.client.controller.Icons
import dbd.core.model.template.ClassLike
import utopia.reflection.component.context.TextContext
import utopia.reflection.component.swing.MultiLineTextView
import utopia.reflection.localization.LocalizedString
import utopia.reflection.util.Screen

import scala.collection.immutable.HashMap

object DeleteQuestionDialog
{
	import dbd.client.view.DefaultContext._
	
	private implicit val language: String = "en"
	
	/**
	 * Creates a personalized delete question dialog
	 * @param deletedItemType Localized name of the deleted item's type
	 * @param deletedItemName Localized name of the deleted item itself
	 * @return A new dialog
	 */
	def personalized(deletedItemType: LocalizedString, deletedItemName: LocalizedString,
					 affectedTypeName: Option[LocalizedString] = None, affectedItems: Vector[String] = Vector()) =
	{
		val messageBuilder = new StringBuilder
		messageBuilder ++= "Are you sure you wish to permanently delete ${type} '${item}'?"
		if (affectedItems.nonEmpty)
		{
			if (affectedTypeName.isDefined)
				messageBuilder ++= "\nThis also affects the following ${affectedType}:"
			else
				messageBuilder ++= "\nThis also affects:"
			
			messageBuilder ++= "\n\t- %s" * affectedItems.size
		}
		val namedArguments = HashMap("type" -> deletedItemType, "item" -> deletedItemName) ++ affectedTypeName.map { "affectedType" -> _ }
		// Combines named & unnamed interpolation
		val question = messageBuilder.result().autoLocalized.interpolated(namedArguments).interpolated(affectedItems)
		new DeleteQuestionDialog("Delete ${type} '${item}'".autoLocalized.interpolated(
			HashMap("type" -> deletedItemType, "item" -> deletedItemName)), question)
	}
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete an attribute
	 * @param attributeName Name of the attribute to delete
	 * @return A new dialog
	 */
	def forAttribute(attributeName: String, affectedClasses: Vector[ClassLike[_, _, _]] = Vector()) =
		DeleteQuestionDialog.personalized("attribute", attributeName.noLanguageLocalizationSkipped,
			Some("classes"), affectedClasses.map { _.name })
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete a class
	 * @param className Name of the class to delete
	 * @return A new dialog
	 */
	def forClass(className: String, affectedClasses: Vector[ClassLike[_, _, _]] = Vector()) =
		DeleteQuestionDialog.personalized("class", className.noLanguageLocalizationSkipped,
			Some("classes"), affectedClasses.map { _.name })
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete a link
	 * @param linkName Name of the link to delete
	 * @return A new dialog
	 */
	def forLink(linkName: String, otherLinkClass: Option[ClassLike[_, _, _]] = None) =
		DeleteQuestionDialog.personalized("link", linkName.noLanguageLocalizationSkipped,
			Some("class"), otherLinkClass.map { _.name }.toVector)
}

/**
 * Used for checking whether the user really wants to delete an item
 * @author Mikko Hilpinen
 * @since 13.1.2020, v0.1
 */
class DeleteQuestionDialog(override val title: LocalizedString, question: LocalizedString)
	extends InteractionDialog[Boolean]
{
	import DeleteQuestionDialog.language
	import dbd.client.view.DefaultContext._
	
	// ATTRIBUTES	------------------------
	
	private implicit val contentContext: TextContext = baseContext.inContextWithBackground(dialogBackground).forTextComponents()
	
	
	// IMPLEMENTED	------------------------
	
	override protected def dialogBackground = InteractionDialog.defaultDialogBackground
	
	override protected def buttonData = Vector(new DialogButtonInfo[Boolean]("Yes", Icons.delete,
		Left(colorScheme.error.invariable), () => Some(true) -> true), DialogButtonInfo.cancel("No"))
	
	override protected def dialogContent = MultiLineTextView.contextual(question, Screen.size.width / 3)
	
	override protected def defaultResult = false
}
