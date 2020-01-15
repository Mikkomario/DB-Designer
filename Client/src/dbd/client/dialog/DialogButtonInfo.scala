package dbd.client.dialog

import dbd.client.controller.Icons
import dbd.client.model.Icon
import utopia.reflection.color.{ColorScheme, ComponentColor}

object DialogButtonInfo
{
	/**
	 * Creates a cancel button
	 * @param rawText Pre-localized text displayed on the button (default = "Cancel")
	 * @param colorScheme Current color scheme (implicit)
	 * @tparam A Type of dialog result
	 * @return A new button info
	 */
	def cancel[A](rawText: String = "Cancel")(implicit colorScheme: ColorScheme) = new DialogButtonInfo[A](rawText,
		Icons.close, colorScheme.primary, () => None -> true)
}

/**
 * Contains necessary data for creating different interactive dialog buttons
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 * @param rawText Pre-localized text for this button
 * @param icon Icon displayed on this button
 * @param color Background color of this button
 * @param generateResultOnPress A function for generating a value when this button is pressed
 *                              (also returns whether the dialog should be closed)
 */
class DialogButtonInfo[+A](val rawText: String, val icon: Icon, val color: ComponentColor,
						   val generateResultOnPress: () => (Option[A], Boolean))
{
	/**
	 * @return Images used with the button
	 */
	def images = icon.forButtonWithBackground(color)
}
