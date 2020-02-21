package dbd.client.dialog

import dbd.client.controller.Icons
import dbd.client.model.Icon
import utopia.reflection.color.{ColorScheme, ComponentColor}
import utopia.reflection.localization.LocalizedString

object DialogButtonInfo
{
	/**
	 * Creates a cancel button
	 * @param text Text displayed on the button
	 * @param colorScheme Current color scheme (implicit)
	 * @tparam A Type of dialog result
	 * @return A new button info
	 */
	def cancel[A](text: LocalizedString)(implicit colorScheme: ColorScheme) = new DialogButtonInfo[A](text,
		Icons.close, colorScheme.primary, () => None -> true)
}

/**
 * Contains necessary data for creating different interactive dialog buttons
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 * @param text Text displayed on this button
 * @param icon Icon displayed on this button
 * @param color Background color of this button
 * @param generateResultOnPress A function for generating a value when this button is pressed
 *                              (also returns whether the dialog should be closed)
 */
class DialogButtonInfo[+A](val text: LocalizedString, val icon: Icon, val color: ComponentColor,
						   val generateResultOnPress: () => (Option[A], Boolean))
{
	/**
	 * @return Images used with the button
	 */
	def images = icon.forButtonWithBackground(color)
}
