package dbd.client.dialog

import utopia.reflection.localization.LocalString._
import dbd.client.controller.Icons
import utopia.reflection.color.ColorScheme
import utopia.reflection.component.swing.MultiLineTextView
import utopia.reflection.localization.{LocalizedString, Localizer}
import utopia.reflection.util.{ComponentContext, Screen}

/**
 * Used for checking whether the user really wants to delete the specified attribute
 * @author Mikko Hilpinen
 * @since 13.1.2020, v0.1
 */
class DeleteAttributeDialog(attributeName: String)
						   (implicit baseContext: ComponentContext, defaultLanguageCode: String, localizer: Localizer,
							colorScheme: ColorScheme)
	extends InteractionDialog[Boolean]
{
	// IMPLEMENTED	------------------------
	
	override protected def buttonData = Vector(new DialogButtonInfo[Boolean]("Yes", Icons.delete,
		colorScheme.error, () => Some(true) -> true), DialogButtonInfo.cancel("No"))
	
	override protected def dialogContent =
	{
		val question: LocalizedString = "Are you sure you wish to permanently delete attribute '%s'?"
		MultiLineTextView.contextual(question.interpolate(attributeName), Screen.size.width / 3)
	}
	
	override protected def defaultResult = false
	
	override protected def title = "Delete Attribute %s".localized.interpolate(attributeName)
}
