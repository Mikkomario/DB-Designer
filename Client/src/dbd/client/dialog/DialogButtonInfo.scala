package dbd.client.dialog

import dbd.client.controller.Icons
import dbd.client.model.Icon
import utopia.reflection.color.ColorSet
import utopia.reflection.component.context.{ButtonContextLike, ColorContextLike}
import utopia.reflection.localization.LocalizedString

object DialogButtonInfo
{
	/**
	 * Creates a cancel button
	 * @param text Text displayed on the button
	 * @tparam A Type of dialog result
	 * @return A new button info
	 */
	def cancel[A](text: LocalizedString) = new DialogButtonInfo[A](text, Icons.close, Right(true), () => None -> true)
}

/**
 * Contains necessary data for creating different interactive dialog buttons
 * @author Mikko Hilpinen
 * @since 15.1.2020, v0.1
 * @param text Text displayed on this button
 * @param icon Icon displayed on this button
 * @param color Background color of this button (either Right: whether button should be of primary (true)
  *              or secondary (false) color or Left: Custom color for the button)
 * @param generateResultOnPress A function for generating a value when this button is pressed
 *                              (also returns whether the dialog should be closed)
 */
class DialogButtonInfo[+A](val text: LocalizedString, val icon: Icon, val color: Either[ColorSet, Boolean],
						   val generateResultOnPress: () => (Option[A], Boolean))
{
	/**
	  * @param context Component creation context
	  * @return Background color that should be used for the button
	  */
	def backgroundColor(implicit context: ColorContextLike) = (color match
	{
		case Right(isPrimary) => if (isPrimary) context.colorScheme.primary else context.colorScheme.secondary
		case Left(custom) => custom
	}).forBackground(context.containerBackground)
	
	/**
	 * @return Images used with the button
	 */
	def images(implicit context: ButtonContextLike) = icon.inButton
}
