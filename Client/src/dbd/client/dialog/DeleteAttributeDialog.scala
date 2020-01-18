package dbd.client.dialog

import utopia.reflection.localization.LocalString._
import utopia.reflection.color.ColorScheme
import utopia.reflection.localization.Localizer
import utopia.reflection.util.ComponentContext

/**
 * Used for checking whether user wishes to delete an attribute
 * @author Mikko Hilpinen
 * @since 18.1.2020, v0.1
 */
object DeleteAttributeDialog
{
	private implicit val language: String = "en"
	
	/**
	 * Creates a new dialog that checks whether the user wishes to delete an attribute
	 * @param attributeName Name of the attribute to delete
	 * @param baseContext Component creation context (implicit)
	 * @param localizer Localizer used (implicit)
	 * @param colorScheme Color scheme (implicit)
	 * @return A new dialog
	 */
	def apply(attributeName: String)
			 (implicit baseContext: ComponentContext, localizer: Localizer, colorScheme: ColorScheme) =
		DeleteQuestionDialog.personalized("attribute", attributeName.noLanguageLocalizationSkipped)
}